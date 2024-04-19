package org.springframework.ai.huaweiai.gallery;

import com.huaweicloud.pangu.dev.sdk.client.gallery.GalleryClient;
import com.huaweicloud.pangu.dev.sdk.client.gallery.chat.GalleryChatResp;
import com.huaweicloud.pangu.dev.sdk.exception.PanguDevSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.huaweiai.gallery.util.ApiUtils;
import org.springframework.ai.huaweiai.gallery.util.LlmUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

public class HuaweiAiGalleryChatClient implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Default options to be used for all chat requests.
     */
    private final HuaweiAiGalleryChatOptions defaultOptions;
    /**
     * 华为 盘古大模型 LLM library.
     */
    private final GalleryClient galleryClient;

    private final RetryTemplate retryTemplate;

    public HuaweiAiGalleryChatClient(GalleryClient galleryClient) {
        this(galleryClient, HuaweiAiGalleryChatOptions.builder()
                        .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                        .withTopP(ApiUtils.DEFAULT_TOP_P)
                        .build());
    }

    public HuaweiAiGalleryChatClient(GalleryClient galleryClient, HuaweiAiGalleryChatOptions options) {
        this(galleryClient, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiGalleryChatClient(GalleryClient galleryClient,
                                     HuaweiAiGalleryChatOptions options,
                                     RetryTemplate retryTemplate) {
        Assert.notNull(galleryClient, "GalleryClient must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.defaultOptions = options;
        this.galleryClient = galleryClient;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        // execute the request
        return retryTemplate.execute(ctx -> {
            Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");
            // Use tenant specific client if available.
            GalleryClient llmClient;
            if(prompt.getOptions() != null && prompt.getOptions() instanceof HuaweiAiGalleryChatTenantOptions chatOptions){
                llmClient = LlmUtils.getOrCreateGalleryClient(chatOptions)
                        .orElseThrow(() -> new PanguDevSDKException("GalleryClient initialization failed for Tenant Request."));
            } else {
                llmClient = this.galleryClient;
            }
            // Ask the model.
            GalleryChatResp panguChatResp;
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                panguChatResp = llmClient.createChat(inputContent);
            } else {
                panguChatResp =  llmClient.createChat(getPromtInstruction(prompt), getHistoryMessages(prompt));
            }
            if (panguChatResp == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }
            return ApiUtils.toChatResponse(panguChatResp);
        });
    }


    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // execute the request
        return retryTemplate.execute(ctx -> Flux.create(sink -> {
            Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");
            // Use tenant specific client if available.
            GalleryClient llmClient;
            if(prompt.getOptions() != null && prompt.getOptions() instanceof HuaweiAiGalleryChatTenantOptions chatOptions){
                llmClient = LlmUtils.getOrCreateGalleryClient(chatOptions)
                        .orElseThrow(() -> new PanguDevSDKException("GalleryClient initialization failed for Tenant Request."));
            } else {
                llmClient = this.galleryClient;
            }
            // Ask the model.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                llmClient.createStreamChat(inputContent, new HuaweiAiGalleryStreamCallBack(sink));
            } else {
                llmClient.createStreamChat(getPromtInstruction(prompt), new HuaweiAiGalleryStreamCallBack(sink), getHistoryMessages(prompt));
            }
        }));
    }

    List<List<String>> getHistoryMessages(Prompt prompt) {
        // Build History list from the prompt.
        var historyMessages = prompt.getInstructions()
                .stream()
                .skip(prompt.getInstructions().size() - 1)
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT
                        || message.getMessageType() == MessageType.SYSTEM)
                .map(m -> Arrays.asList(m.getContent()))
                .toList();
        return historyMessages;
    }

    String getPromtInstruction(Prompt prompt) {

        var userMessages =  prompt.getInstructions()
                .stream()
                .filter(message -> message.getMessageType() == MessageType.USER)
                .toList();
        var promtInstruction = CollectionUtils.lastElement(userMessages).getContent();
        return promtInstruction;
    }

}
