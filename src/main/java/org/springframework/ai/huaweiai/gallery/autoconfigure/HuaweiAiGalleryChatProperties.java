package org.springframework.ai.huaweiai.gallery.autoconfigure;

import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryChatOptions;
import org.springframework.ai.huaweiai.gallery.util.ApiUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HuaweiAiGalleryChatProperties.CONFIG_PREFIX)
public class HuaweiAiGalleryChatProperties {

    public static final String CONFIG_PREFIX = "spring.ai.huaweiai.gallery.chat";


    /**
     * Enable Huawei Gallery chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever Huawei Gallery options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private HuaweiAiGalleryChatOptions options = HuaweiAiGalleryChatOptions.builder()
            .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
            .withTopP(ApiUtils.DEFAULT_TOP_P)
            .build();

    public HuaweiAiGalleryChatOptions getOptions() {
        return this.options;
    }

    public void setOptions(HuaweiAiGalleryChatOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
