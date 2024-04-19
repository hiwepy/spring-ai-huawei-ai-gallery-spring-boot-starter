package org.springframework.ai.huaweiai.gallery.autoconfigure;

import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.client.gallery.GalleryClient;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryCachedChatClient;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryChatClient;
import org.springframework.ai.huaweiai.gallery.util.ApiUtils;
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

import java.util.Objects;

/**
 * {@link AutoConfiguration Auto-configuration} for Huawei Gallery Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ HuaweiAiGalleryChatProperties.class, HuaweiAiGalleryConnectionProperties.class, HuaweiAiGalleryIamProperties.class })
@ConditionalOnClass(LLMs.class)
public class HuaweiAiGalleryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiGalleryChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    public LLMConfig llmConfig(HuaweiAiGalleryConnectionProperties connectionProperties,
                               HuaweiAiGalleryChatProperties chatProperties,
                               HuaweiAiGalleryIamProperties iamProperties) {
        LLMConfig llmConfig = LLMConfig.builder()
                .iamConfig(ApiUtils.toIAMConfig(iamProperties))
                .build();
        if(Objects.nonNull(connectionProperties.getHttpProxy())){
            llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(connectionProperties.getHttpProxy()));
        }
        if(Objects.nonNull(chatProperties.getOptions())){
            llmConfig.setLlmParamConfig(ApiUtils.toLLMParamConfig(chatProperties.getOptions()));
            llmConfig.setLlmModuleConfig(ApiUtils.toLLMModuleConfig(chatProperties, connectionProperties));
        }
        return llmConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiGalleryChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    public HuaweiAiGalleryChatClient panguAiChatClient(LLMConfig llmConfig,
                                                       HuaweiAiGalleryChatProperties chatProperties,
                                                       ObjectProvider<RetryTemplate> retryTemplateProvider) {
        GalleryClient galleryClient = new GalleryClient(llmConfig);
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiGalleryChatClient(galleryClient, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiGalleryChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true")
    public HuaweiAiGalleryCachedChatClient panguAiCachedChatClient(LLMConfig llmConfig,
                                                                   HuaweiAiGalleryChatProperties chatProperties,
                                                                   ObjectProvider<RetryTemplate> retryTemplateProvider) {
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiGalleryCachedChatClient(llmConfig, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
