package org.springframework.ai.huaweiai.gallery.autoconfigure;


import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryCachedChatClient;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryChatClient;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryEmbeddingClient;
import org.springframework.ai.huaweiai.gallery.util.ApiUtils;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Huawei Gallery Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ HuaweiAiGalleryChatProperties.class, HuaweiAiGalleryConnectionProperties.class, HuaweiAiGalleryEmbeddingProperties.class,
        HuaweiAiGalleryIamProperties.class })
@ConditionalOnClass(LLMs.class)
public class HuaweiAiGalleryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLMConfig llmConfig(HuaweiAiGalleryConnectionProperties connectionProperties,
                               HuaweiAiGalleryChatProperties chatProperties,
                               HuaweiAiGalleryIamProperties iamProperties) {
        LLMConfig llmConfig = new LLMConfig();
        llmConfig.setLlmModuleConfig(ApiUtils.toLLMModuleConfig(connectionProperties));
        llmConfig.setLlmParamConfig(ApiUtils.toLLMParamConfig(chatProperties.getOptions()));
        llmConfig.setIamConfig(ApiUtils.toIAMConfig(iamProperties));
        llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(connectionProperties.getHttpProxy()));
        return llmConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamCallBack streamCallBack() {
        return ApiUtils.DEFAULT_STREAM_CALLBACK;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiGalleryChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiGalleryChatClient galleryAiChatClient(LLMConfig llmConfig,
                                                     HuaweiAiGalleryChatProperties chatProperties,
                                                     List<FunctionCallback> toolFunctionCallbacks,
                                                     ObjectProvider<StreamCallBack> streamCallBackProvider,
                                                     ObjectProvider<RetryTemplate> retryTemplateProvider) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        GalleryClient galleryClient = new GalleryClient(llmConfig);
        StreamCallBack streamCallBack = streamCallBackProvider.getIfAvailable(() -> ApiUtils.DEFAULT_STREAM_CALLBACK);
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiGalleryChatClient(galleryClient, streamCallBack, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiGalleryChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiGalleryCachedChatClient galleryAiCachedChatClient(LLMConfig llmConfig,
                                                     HuaweiAiGalleryChatProperties chatProperties,
                                                     List<FunctionCallback> toolFunctionCallbacks,
                                                     ObjectProvider<StreamCallBack> streamCallBackProvider,
                                                     ObjectProvider<RetryTemplate> retryTemplateProvider) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }

        LLM llm = LLMs.of(LLMs.PANGU, llmConfig);
        // TODO 增加缓存配置
        //llm.setCache(Caches.of(Caches.IN_MEMORY));
        StreamCallBack streamCallBack = streamCallBackProvider.getIfAvailable(() -> ApiUtils.DEFAULT_STREAM_CALLBACK);
        llm.setStreamCallback(streamCallBack);
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiGalleryCachedChatClient((Gallery) llm, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiGalleryEmbeddingClient galleryAiEmbeddingClient(LLMConfig llmConfig,
                                                               HuaweiAiGalleryEmbeddingProperties embeddingProperties,
                                                               ObjectProvider<RetryTemplate> retryTemplateProvider) {
        GalleryClient galleryClient = new GalleryClient(llmConfig);
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiGalleryEmbeddingClient(galleryClient, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
