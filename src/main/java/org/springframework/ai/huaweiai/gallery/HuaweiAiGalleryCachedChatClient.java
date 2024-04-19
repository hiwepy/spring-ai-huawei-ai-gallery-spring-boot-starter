package org.springframework.ai.huaweiai.gallery;

import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMParamConfig;
import com.huaweicloud.pangu.dev.sdk.client.gallery.chat.GalleryChatResp;
import com.huaweicloud.pangu.dev.sdk.exception.PanguDevSDKException;
import com.huaweicloud.pangu.dev.sdk.llms.module.Gallery;
import com.huaweicloud.pangu.dev.sdk.llms.response.LLMRespGallery;
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
import org.springframework.ai.huaweiai.gallery.util.LlmUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;

public class HuaweiAiGalleryCachedChatClient implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private final HuaweiAiGalleryChatOptions defaultOptions;
    /**
     * 华为 盘古大模型 LLM Config.
     */
    private final LLMConfig defaultLlmConfig;
    private final Gallery pangu;
    private final RetryTemplate retryTemplate;

    public HuaweiAiGalleryCachedChatClient(LLMConfig llmConfig) {
        this(llmConfig, HuaweiAiGalleryChatOptions.builder()
                .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                .withTopP(ApiUtils.DEFAULT_TOP_P)
                .build());
    }

    public HuaweiAiGalleryCachedChatClient(LLMConfig llmConfig, HuaweiAiGalleryChatOptions options) {
        this(llmConfig, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiGalleryCachedChatClient(LLMConfig llmConfig,
                                         HuaweiAiGalleryChatOptions options,
                                         RetryTemplate retryTemplate) {
        Assert.notNull(llmConfig, "LLMConfig must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.defaultOptions = options;
        this.defaultLlmConfig = llmConfig;
        this.pangu = LlmUtils.createLlm(llmConfig);
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");
        // execute the request
        return retryTemplate.execute(ctx -> {

            // Use tenant specific client if available.
            Gallery llm;
            if(prompt.getOptions() != null && prompt.getOptions() instanceof HuaweiAiGalleryChatTenantOptions chatOptions){
                // Create the Gallery LLM for Tenant.
                llm = LlmUtils.getOrCreateGalleryLLM(chatOptions)
                        .orElseThrow(() -> new PanguDevSDKException("Gallery LLM initialization failed for Tenant Request."));
            } else {
                // Use the default client.
                llm = this.pangu;
            }

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
            mergedOptions.setStream(Boolean.FALSE);

            // Build LLMParamConfig from the merged options.
            LLMParamConfig paramConfig = ApiUtils.toLLMParamConfig(mergedOptions);

            // Ask the model.
            LLMRespGallery panguChatResp;
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                panguChatResp = llm.ask(inputContent, paramConfig);
            } else {
                panguChatResp = llm.ask(ApiUtils.toConversationMessage(prompt.getInstructions()), paramConfig);
            }
            if (panguChatResp == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }
            return this.toChatCompletion(panguChatResp);
        });
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {

        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");

        // execute the request
        return retryTemplate.execute(ctx -> Flux.create(sink -> {

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
            mergedOptions.setStream(Boolean.TRUE);

            // Use tenant specific client if available.
            LLM llm;
            if(prompt.getOptions() != null && prompt.getOptions() instanceof HuaweiAiGalleryChatTenantOptions chatOptions){
                // Build LLMConfig from the chat options.
                LLMConfig llmConfig = LLMConfig.builder()
                        .httpConfig(ApiUtils.toHTTPConfig(chatOptions.getHttpProxyOptions()))
                        .iamConfig(ApiUtils.toIAMConfig(chatOptions.getIamOptions()))
                        .llmParamConfig(ApiUtils.toLLMParamConfig(chatOptions))
                        .llmModuleConfig(ApiUtils.toLLMModuleConfig(chatOptions.getModuleOptions()))
                        .build();
                // Create the LLM.
                llm = LLMs.of(LLMs.GALLERY, llmConfig);
            } else {
                // Build LLMConfig from the merged options.
                LLMConfig llmConfig = LLMConfig.builder()
                        .llmParamConfig(ApiUtils.toLLMParamConfig(mergedOptions))
                        .build();
                // Merge the default LLMConfig with the merged options.
                LLMConfig mergedLLMConfig = ModelOptionsUtils.merge(llmConfig, this.defaultLlmConfig, LLMConfig.class);
                // Create the LLM.
                llm = LLMs.of(LLMs.GALLERY, mergedLLMConfig);
            }
            llm.setStreamCallback(new HuaweiAiGalleryStreamCallBack(sink));
            // Ask the model.
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                llm.ask(inputContent);
            } else {
                llm.ask(ApiUtils.toConversationMessage(prompt.getInstructions()));
            }
        }));
    }

    private ChatResponse toChatCompletion(LLMRespGallery chunk) {

        GalleryChatResp resp = chunk.getGalleryChatResp();

        List<Generation> generations = resp.getChoices()
                .stream()
                .map(choice -> new Generation(choice.getText())
                        .withGenerationMetadata(ChatGenerationMetadata.from("chat.completion", ApiUtils.extractUsage(resp))))
                .toList();

        return new ChatResponse(generations, HuaweiAiGalleryChatResponseMetadata.from(resp));
    }

}
