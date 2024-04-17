package org.springframework.ai.huaweiai.gallery.autoconfigure;

import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.api.memory.cache.Caches;
import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryChatClient;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryEmbeddingClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for 百度千帆 Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ HuaweiAiGalleryChatProperties.class, HuaweiAiGalleryConnectionProperties.class, HuaweiAiGalleryEmbeddingProperties.class })
@ConditionalOnClass(LLMs.class)
public class HuaweiAiGalleryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLM llm(HuaweiAiGalleryConnectionProperties properties) {
        //Assert.isNull(properties.getType(), "llm Type must be set");
        Assert.hasText(properties.getAccessKey(), "llm API Access Key must be set");
        Assert.hasText(properties.getSecretKey(), "llm API Secret Key must be set");

        LLMConfig llmConfig = new LLMConfig();
        LLM llm = LLMs.of(LLMs.PANGU, llmConfig);
        llm.setCache(Caches.of(Caches.IN_MEMORY));

        return llm;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiGalleryChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiGalleryChatClient panguAiChatClient(LLM llm,
                                                       HuaweiAiGalleryChatProperties chatProperties,
                                                       List<FunctionCallback> toolFunctionCallbacks,
                                                       FunctionCallbackContext functionCallbackContext) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        return new HuaweiAiGalleryChatClient(llm, chatProperties.getOptions(), functionCallbackContext);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiGalleryEmbeddingClient panguAiEmbeddingClient(LLM llm, HuaweiAiGalleryEmbeddingProperties embeddingProperties) {

        return new HuaweiAiGalleryEmbeddingClient(llm, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions());
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
