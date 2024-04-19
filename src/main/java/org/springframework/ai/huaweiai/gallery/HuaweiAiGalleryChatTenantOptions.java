package org.springframework.ai.huaweiai.gallery;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HuaweiAiGalleryChatTenantOptions extends HuaweiAiGalleryChatOptions {

    /**
     * IAM 配置，用于接口运行期间动态修改 IAM 信息，方便支持多租户场景
     */
    @JsonProperty(value = "iamOptions")
    private HuaweiAiGalleryIamOptions iamOptions;
    /**
     * 模型配置，用于接口运行期间动态修改模型信息，方便支持多租户场景
     */
    @JsonProperty(value = "moduleOptions")
    private HuaweiAiGalleryModuleOptions moduleOptions;
    /**
     * 网络代理配置，用于接口运行期间动态修改网络代理信息，方便支持多租户场景
     */
    @JsonProperty(value = "httpProxyOptions")
    private HuaweiAiGalleryHttpProxyOptions httpProxyOptions;

    public static TenantBuilder tenantBuilder() {
        return new TenantBuilder();
    }

    public static class TenantBuilder {

        private HuaweiAiGalleryChatTenantOptions options;

        public TenantBuilder() {
            this.options = new HuaweiAiGalleryChatTenantOptions();
        }

        public TenantBuilder withMaxToken(Integer maxTokens) {
            this.options.setMaxTokens(maxTokens);
            return this;
        }

        public TenantBuilder withTemperature(Float temperature) {
            this.options.setTemperature(temperature);
            return this;
        }

        public TenantBuilder withTopP(Float topP) {
            this.options.setTopP(topP);
            return this;
        }

        public TenantBuilder withPenaltyScore(Double penaltyScore) {
            this.options.setPenaltyScore(penaltyScore);
            return this;
        }

        public TenantBuilder withSystem(String system) {
            this.options.setSystem(system);
            return this;
        }

        public TenantBuilder withUser(String user) {
            this.options.setUser(user);
            return this;
        }

        public TenantBuilder withStop(List<String> stop) {
            this.options.setStop(stop);
            return this;
        }

        public TenantBuilder withAnswerNum(Integer answerNum) {
            this.options.setAnswerNum(answerNum);
            return this;
        }

        public TenantBuilder withPresencePenalty(Double presencePenalty) {
            this.options.setPresencePenalty(presencePenalty);
            return this;
        }

        public TenantBuilder withFrequencyPenalty(Double frequencyPenalty) {
            this.options.setFrequencyPenalty(frequencyPenalty);
            return this;
        }

        public TenantBuilder withWithPrompt(Boolean withPrompt) {
            this.options.setWithPrompt(withPrompt);
            return this;
        }

        public TenantBuilder withBestOf(Integer bestOf) {
            this.options.setBestOf(bestOf);
            return this;
        }

        public TenantBuilder withStream(Boolean stream) {
            this.options.setStream(stream);
            return this;
        }

        public TenantBuilder withIamOptions(HuaweiAiGalleryIamOptions iamOptions) {
            this.options.setIamOptions(iamOptions);
            return this;
        }

        public TenantBuilder withModuleOptions(HuaweiAiGalleryModuleOptions moduleOptions) {
            this.options.setModuleOptions(moduleOptions);
            return this;
        }

        public TenantBuilder withHttpProxyOptions(HuaweiAiGalleryHttpProxyOptions httpProxyOptions) {
            this.options.setHttpProxyOptions(httpProxyOptions);
            return this;
        }

        public HuaweiAiGalleryChatTenantOptions build() {
            return this.options;
        }

    }

    public HuaweiAiGalleryIamOptions getIamOptions() {
        return iamOptions;
    }

    public HuaweiAiGalleryModuleOptions getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(HuaweiAiGalleryModuleOptions moduleOptions) {
        this.moduleOptions = moduleOptions;
    }

    public void setIamOptions(HuaweiAiGalleryIamOptions iamOptions) {
        this.iamOptions = iamOptions;
    }

    public HuaweiAiGalleryHttpProxyOptions getHttpProxyOptions() {
        return httpProxyOptions;
    }

    public void setHttpProxyOptions(HuaweiAiGalleryHttpProxyOptions httpProxyOptions) {
        this.httpProxyOptions = httpProxyOptions;
    }

}
