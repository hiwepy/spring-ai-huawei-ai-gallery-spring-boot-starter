package org.springframework.ai.huaweiai.gallery;

import com.huaweicloud.gallery.dev.sdk.api.llms.config.LLMParamConfig;
import com.huaweicloud.pangu.dev.sdk.client.gallery.chat.GalleryChatResp;
import com.huaweicloud.gallery.dev.sdk.llms.module.Gallery;
import com.huaweicloud.gallery.dev.sdk.llms.response.LLMRespGallery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.huaweiai.gallery.metadata.HuaweiAiGalleryChatResponseMetadata;
import org.springframework.ai.huaweiai.gallery.util.ApiUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

public class HuaweiAiGalleryCachedChatClient implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private HuaweiAiGalleryChatOptions defaultOptions;
    /**
     * 华为 盘古大模型 LLM library.
     */
    private final Gallery gallery;

    private final RetryTemplate retryTemplate;

    public HuaweiAiGalleryCachedChatClient(Gallery gallery) {
        this(gallery, HuaweiAiGalleryChatOptions.builder()
                .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                .withTopP(ApiUtils.DEFAULT_TOP_P)
                .build());
    }

    public HuaweiAiGalleryCachedChatClient(Gallery gallery, HuaweiAiGalleryChatOptions options) {
        this(gallery, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiGalleryCachedChatClient(Gallery gallery,
                                           HuaweiAiGalleryChatOptions options,
                                           RetryTemplate retryTemplate) {
        Assert.notNull(gallery, "Gallery must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.gallery = gallery;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");
        return retryTemplate.execute(ctx -> {

            // runtime options
            HuaweiAiGalleryChatOptions runtimeOptions = null;
            if (prompt.getOptions() != null) {
                if (prompt.getOptions() instanceof ChatOptions runtimeChatOptions) {
                    runtimeOptions = ModelOptionsUtils.copyToTarget(runtimeChatOptions, ChatOptions.class, HuaweiAiGalleryChatOptions.class);
                }
                else {
                    throw new IllegalArgumentException("Prompt options are not of type ChatOptions: " + prompt.getOptions().getClass().getSimpleName());
                }
            }

            // Merge runtime options with default options.
            HuaweiAiGalleryChatOptions mergedOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions, HuaweiAiGalleryChatOptions.class);

            // Build LLMParamConfig from the merged options.
            LLMParamConfig paramConfig = LLMParamConfig.builder()
                    .maxTokens(mergedOptions.getMaxTokens())
                    .n(mergedOptions.getAnswerNum())
                    .temperature(Objects.nonNull(mergedOptions.getTemperature()) ? mergedOptions.getTemperature().doubleValue() : null)
                    .topP(Objects.nonNull(mergedOptions.getTopP()) ? mergedOptions.getTopP().doubleValue() : null)
                    .presencePenalty(mergedOptions.getPresencePenalty())
                    .withPrompt(mergedOptions.getWithPrompt())
                    .stream(Boolean.FALSE)
                    .build();

            // Ask the model.
            LLMRespGallery galleryChatResp = null;
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                galleryChatResp = gallery.ask(inputContent, paramConfig);
            } else {
                galleryChatResp = this.gallery.ask(ApiUtils.toConversationMessage(prompt.getInstructions()), paramConfig);
            }
            if (galleryChatResp == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }

            return this.toChatCompletion(galleryChatResp);
        });
    }


    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {

        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");

        return retryTemplate.execute(ctx -> {

            // runtime options
            HuaweiAiGalleryChatOptions runtimeOptions = null;
            if (prompt.getOptions() != null) {
                if (prompt.getOptions() instanceof ChatOptions runtimeChatOptions) {
                    runtimeOptions = ModelOptionsUtils.copyToTarget(runtimeChatOptions, ChatOptions.class, HuaweiAiGalleryChatOptions.class);
                }
                else {
                    throw new IllegalArgumentException("Prompt options are not of type ChatOptions: " + prompt.getOptions().getClass().getSimpleName());
                }
            }

            // Merge runtime options with default options.
            HuaweiAiGalleryChatOptions mergedOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions, HuaweiAiGalleryChatOptions.class);

            // Build LLMParamConfig from the merged options.
            LLMParamConfig paramConfig = LLMParamConfig.builder()
                    .maxTokens(mergedOptions.getMaxTokens())
                    .n(mergedOptions.getAnswerNum())
                    .temperature(Objects.nonNull(mergedOptions.getTemperature()) ? mergedOptions.getTemperature().doubleValue() : null)
                    .topP(Objects.nonNull(mergedOptions.getTopP()) ? mergedOptions.getTopP().doubleValue() : null)
                    .presencePenalty(mergedOptions.getPresencePenalty())
                    .withPrompt(mergedOptions.getWithPrompt())
                    .stream(Boolean.TRUE)
                    .build();

            // Ask the model.
            LLMRespGallery galleryChatResp = null;
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                galleryChatResp = gallery.ask(inputContent, paramConfig);
            } else {
                galleryChatResp = this.gallery.ask(ApiUtils.toConversationMessage(prompt.getInstructions()), paramConfig);
            }
            if (galleryChatResp == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return Flux.empty();
            }

            return Flux.just(toChatCompletion(galleryChatResp)) ;
        });
    }

    private ChatResponse toChatCompletion(LLMRespGallery chunk) {

        GalleryChatResp resp = chunk.getGalleryChatResp();

        List<Generation> generations = resp.getChoices()
                .stream()
                .map(choice -> new Generation(choice.getMessage().getContent(), ApiUtils.toMap(resp.getId(), choice))
                        .withGenerationMetadata(ChatGenerationMetadata.from("chat.completion", ApiUtils.extractUsage(resp))))
                .toList();

        return new ChatResponse(generations, HuaweiAiGalleryChatResponseMetadata.from(resp));
    }

}
