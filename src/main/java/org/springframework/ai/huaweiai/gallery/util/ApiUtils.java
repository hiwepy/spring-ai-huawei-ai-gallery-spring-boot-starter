package org.springframework.ai.huaweiai.gallery.util;

import com.huaweicloud.pangu.dev.sdk.api.config.HTTPConfig;
import com.huaweicloud.pangu.dev.sdk.api.config.IAMConfig;
import com.huaweicloud.pangu.dev.sdk.api.embedings.config.EmbeddingConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMModuleConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMParamConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.ConversationMessage;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.Role;
import com.huaweicloud.pangu.dev.sdk.api.llms.response.LLMResp;
import com.huaweicloud.pangu.dev.sdk.client.gallery.chat.GalleryChatResp;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.huaweiai.gallery.*;
import org.springframework.ai.huaweiai.gallery.autoconfigure.HuaweiAiGalleryChatProperties;
import org.springframework.ai.huaweiai.gallery.autoconfigure.HuaweiAiGalleryConnectionProperties;
import org.springframework.ai.huaweiai.gallery.autoconfigure.HuaweiAiGalleryHttpProxyProperties;
import org.springframework.ai.huaweiai.gallery.autoconfigure.HuaweiAiGalleryIamProperties;
import org.springframework.ai.huaweiai.gallery.metadata.HuaweiAiGalleryChatResponseMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

public class ApiUtils {

    public static final Float DEFAULT_TEMPERATURE = 0.95f;
    public static final Float DEFAULT_TOP_P = 1.0f;

    public static LLMModuleConfig toLLMModuleConfig(HuaweiAiGalleryModuleOptions moduleOptions){
        if (Objects.isNull(moduleOptions)) {
            return null;
        }
        LLMModuleConfig httpConfig = LLMModuleConfig.builder()
                .LLMName(LLMs.GALLERY)
                .url(moduleOptions.getUrl())
                .moduleVersion(moduleOptions.getModuleVersion())
                .enableAppendSystemMessage(moduleOptions.isEnableAppendSystemMessage())
                .systemPrompt(moduleOptions.getSystemPrompt())
                .build();
        return httpConfig;
    }

    public static LLMModuleConfig toLLMModuleConfig(HuaweiAiGalleryChatProperties chatProperties, HuaweiAiGalleryConnectionProperties connectionProperties){
        if (Objects.isNull(connectionProperties)) {
            return null;
        }
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : connectionProperties.getBaseUrl();
        Assert.hasText(baseUrl, "Huawei AI Gallery base URL must be set");

        LLMModuleConfig httpConfig = LLMModuleConfig.builder()
                //.LLMName(LLMs.GALLERY)
                .url(baseUrl)
                .moduleVersion(connectionProperties.getModuleVersion())
                .enableAppendSystemMessage(connectionProperties.isEnableAppendSystemMessage())
                .systemPrompt(connectionProperties.getSystemPrompt())
                .build();
        return httpConfig;
    }

    public static LLMParamConfig toLLMParamConfig(HuaweiAiGalleryChatOptions options){
        if (Objects.isNull(options)) {
            return null;
        }
        LLMParamConfig llmParamConfig = LLMParamConfig.builder()
                .maxTokens(options.getMaxTokens())
                .temperature(Objects.nonNull(options.getTemperature()) ?  options.getTemperature().doubleValue() : null)
                .topP(Objects.nonNull(options.getTopP()) ?  options.getTopP().doubleValue() : null)
                .presencePenalty(options.getPresencePenalty())
                .frequencyPenalty(options.getFrequencyPenalty())
                .withPrompt(options.getWithPrompt())
                .stream(Objects.nonNull(options.getStream()) ? options.getStream() : Boolean.FALSE)
                .build();
        if(Objects.nonNull(options.getBestOf())){
            llmParamConfig.setBestOf(options.getBestOf());
        }
        return llmParamConfig;
    }

    public static IAMConfig toIAMConfig(HuaweiAiGalleryIamProperties iamProperties){
        if (Objects.isNull(iamProperties)) {
            return null;
        }
        IAMConfig iamConfig = IAMConfig.builder()
                .iamUrl(iamProperties.getUrl())
                .iamDomain(iamProperties.getDomain())
                .iamUser(iamProperties.getUser())
                .iamPwd(iamProperties.getPassword())
                .projectName(iamProperties.getProjectName())
                .disabled(iamProperties.getDisabled())
                .ak(iamProperties.getAk())
                .sk(iamProperties.getSk())
                .build();
        if(Objects.nonNull(iamProperties.getHttpProxy())){
            iamConfig.setHttpConfig(ApiUtils.toHTTPConfig(iamProperties.getHttpProxy()));
        }
        return iamConfig;
    }

    public static IAMConfig toIAMConfig(HuaweiAiGalleryIamOptions iamOptions){
        if (Objects.isNull(iamOptions)) {
            return null;
        }
        return IAMConfig.builder()
                .iamUrl(iamOptions.getUrl())
                .iamDomain(iamOptions.getDomain())
                .iamUser(iamOptions.getUser())
                .iamPwd(iamOptions.getPassword())
                .projectName(iamOptions.getProjectName())
                .ak(iamOptions.getAk())
                .sk(iamOptions.getSk())
                .build();
    }

    public static HTTPConfig toHTTPConfig(HuaweiAiGalleryHttpProxyOptions httpProxyOptions){
        if (Objects.isNull(httpProxyOptions)) {
            return null;
        }
        HTTPConfig httpConfig = HTTPConfig.builder()
                .asyncHttpWaitSeconds(httpProxyOptions.getAsyncHttpWaitSeconds())
                .proxyEnabled(Optional.ofNullable(httpProxyOptions.getProxyEnabled()).orElse(Boolean.FALSE))
                .proxyUrl(httpProxyOptions.getProxyUrl())
                .proxyUser(httpProxyOptions.getProxyUser())
                .proxyPassword(httpProxyOptions.getProxyPassword())
                .build();
        return httpConfig;
    }

    public static HTTPConfig toHTTPConfig(HuaweiAiGalleryHttpProxyProperties proxyProperties){
        if (Objects.isNull(proxyProperties)) {
            return null;
        }
        HTTPConfig httpConfig = HTTPConfig.builder()
                .asyncHttpWaitSeconds(proxyProperties.getAsyncHttpWaitSeconds())
                .proxyEnabled(Optional.ofNullable(proxyProperties.getProxyEnabled()).orElse(Boolean.FALSE))
                .proxyUrl(proxyProperties.getProxyUrl())
                .proxyUser(proxyProperties.getProxyUser())
                .proxyPassword(proxyProperties.getProxyPassword())
                .build();
        return httpConfig;
    }

    public static List<ConversationMessage> toConversationMessage(List<Message> messages){
        if(Objects.isNull(messages)){
            return Collections.emptyList();
        }
        // Build ConversationMessage list from the prompt.
        return messages.stream()
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT
                        || message.getMessageType() == MessageType.SYSTEM)
                .map(m -> ConversationMessage.builder().role(ApiUtils.toRole(m)).content(m.getContent()).build())
                .toList();
    }

    public static Role toRole(Message message) {
        switch (message.getMessageType()) {
            case USER:
                return Role.USER;
            case ASSISTANT:
                return Role.ASSISTANT;
            case SYSTEM:
                return Role.SYSTEM;
            default:
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
        }
    }

    public static Map<String, Object> toMap(String callBackId, LLMResp llmResp) {
        Map<String, Object> map = new HashMap<>();
        map.put("isFromCache", llmResp.isFromCache());
        map.put("callBackId", callBackId);
        return map;
    }

    public static ChatResponse toChatResponse(GalleryChatResp resp) {
        List<Generation> generations = resp.getChoices()
                .stream()
                .map(choice -> new Generation(choice.getText())
                        .withGenerationMetadata(ChatGenerationMetadata.from("chat.completion", ApiUtils.extractUsage(resp))))
                .toList();
        return new ChatResponse(generations, HuaweiAiGalleryChatResponseMetadata.from(resp));
    }

    public static ChatResponse toChatResponse(String callBackId, LLMResp llmResp, boolean completion) {
        List<Generation> generations = Arrays.asList(new Generation(llmResp.getAnswer(), ApiUtils.toMap(callBackId, llmResp))
                .withGenerationMetadata( completion ? ChatGenerationMetadata.from("chat.completion", null) : ChatGenerationMetadata.NULL));
        return new ChatResponse(generations);
    }

    public static Usage extractUsage(GalleryChatResp response) {
        return new Usage() {

            @Override
            public Long getPromptTokens() {
                return response.getUsage().getPromptTokens();
            }

            @Override
            public Long getGenerationTokens() {
                return response.getUsage().getCompletionTokens();
            }

            @Override
            public Long getTotalTokens() {
                return response.getUsage().getTotalTokens();
            }
        };
    }

    public static EmbeddingConfig toEmbeddingConfig(HuaweiAiGalleryChatTenantOptions tenantOptions) {
        return EmbeddingConfig.builder()
                .iamConfig(toIAMConfig(tenantOptions.getIamOptions()))
                .httpConfig(toHTTPConfig(tenantOptions.getHttpProxyOptions()))
                .build();
    }
}
