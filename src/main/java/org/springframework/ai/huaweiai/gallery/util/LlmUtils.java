package org.springframework.ai.huaweiai.gallery.util;

import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.client.gallery.GalleryClient;
import com.huaweicloud.pangu.dev.sdk.llms.module.Gallery;
import com.huaweicloud.pangu.dev.sdk.utils.SecurityUtil;
import org.springframework.ai.huaweiai.gallery.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class LlmUtils {

    private static Map<String, Optional<Gallery>> LLM_MAP = new ConcurrentHashMap<>();
    private static Map<String, Optional<GalleryClient>> LLM_CHAT_CLIENT_MAP = new ConcurrentHashMap<>();

    public static Gallery createLlm(LLMConfig llmConfig){
        return (Gallery) LLMs.of(LLMs.GALLERY, llmConfig);
    }

    /**
     * 获取联合键
     * @param chatOptions
     * @return
     */
    public static String getUnionKey(HuaweiAiGalleryChatOptions chatOptions) {
        if(chatOptions instanceof HuaweiAiGalleryChatTenantOptions tenantOptions){
            StringJoiner joiner = new StringJoiner(".");
            HuaweiAiGalleryIamOptions iamOptions = tenantOptions.getIamOptions();
            if(Objects.nonNull(iamOptions)){
                joiner.add(iamOptions.getUnionKey());
            }
            HuaweiAiGalleryModuleOptions moduleOptions = tenantOptions.getModuleOptions();
            if(Objects.nonNull(moduleOptions)){
                joiner.add(moduleOptions.getUnionKey());
            }
            HuaweiAiGalleryHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
            if(Objects.nonNull(httpProxyOptions)){
                joiner.add(httpProxyOptions.getUnionKey());
            }
            return joiner.toString();
        }
        return SecurityUtil.getUnionKey(LLMs.GALLERY);
    }

    /**
     * 获取或创建 Gallery
     * @param chatOptions 聊天配置
     * @return Gallery
     */
    public static Optional<Gallery> getOrCreateGalleryLLM(HuaweiAiGalleryChatOptions chatOptions) {
        return LLM_MAP.computeIfAbsent(LlmUtils.getUnionKey(chatOptions), unionKey -> {
            // 判断是否为独立租户聊天配置
            if(chatOptions instanceof HuaweiAiGalleryChatTenantOptions tenantOptions){
                // 如果没有实时指定IAM配置、模型配置，则认为无需区分 LLM 客户端
                HuaweiAiGalleryIamOptions iamOptions = tenantOptions.getIamOptions();
                HuaweiAiGalleryModuleOptions moduleOptions = tenantOptions.getModuleOptions();
                if(Objects.isNull(iamOptions) && Objects.isNull(moduleOptions) ){
                    return Optional.empty();
                }
                // 构建LLMConfig
                LLMConfig llmConfig = LLMConfig.builder()
                        .iamConfig(ApiUtils.toIAMConfig(iamOptions))
                        .llmParamConfig(ApiUtils.toLLMParamConfig(tenantOptions))
                        .llmModuleConfig(ApiUtils.toLLMModuleConfig(moduleOptions))
                        .build();
                HuaweiAiGalleryHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
                if(Objects.nonNull(httpProxyOptions)){
                    llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(httpProxyOptions));
                }
                // 构建LLM
                LLM pangu = LLMs.of(LLMs.GALLERY, llmConfig);
                // TODO 增加缓存配置
                //llm.setCache(Caches.of(Caches.IN_MEMORY));
                return Optional.ofNullable((Gallery) pangu);
            }
            return Optional.empty();
        });
    }

    /**
     * 获取或创建 GalleryClient
     * @param chatOptions 聊天配置
     * @return GalleryClient
     */
    public static Optional<GalleryClient> getOrCreateGalleryClient(HuaweiAiGalleryChatOptions chatOptions) {
        return LLM_CHAT_CLIENT_MAP.computeIfAbsent(LlmUtils.getUnionKey(chatOptions), unionKey -> {
            // 判断是否为独立租户聊天配置
            if(chatOptions instanceof HuaweiAiGalleryChatTenantOptions tenantOptions){
                // 如果没有实时指定IAM配置、模型配置，则认为无需区分 LLM 客户端
                HuaweiAiGalleryIamOptions iamOptions = tenantOptions.getIamOptions();
                HuaweiAiGalleryModuleOptions moduleOptions = tenantOptions.getModuleOptions();
                if(Objects.isNull(iamOptions) && Objects.isNull(moduleOptions) ){
                    return Optional.empty();
                }
                // 构建LLMConfig
                LLMConfig llmConfig = LLMConfig.builder()
                        .iamConfig(ApiUtils.toIAMConfig(iamOptions))
                        .llmParamConfig(ApiUtils.toLLMParamConfig(tenantOptions))
                        .llmModuleConfig(ApiUtils.toLLMModuleConfig(moduleOptions))
                        .build();
                HuaweiAiGalleryHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
                if(Objects.nonNull(httpProxyOptions)){
                    llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(httpProxyOptions));
                }

                return Optional.ofNullable(new GalleryClient(llmConfig));
            }
            return Optional.empty();
        });
    }

}
