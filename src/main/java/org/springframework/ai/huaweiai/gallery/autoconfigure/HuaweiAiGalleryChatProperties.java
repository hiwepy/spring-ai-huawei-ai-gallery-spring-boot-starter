package org.springframework.ai.huaweiai.gallery.autoconfigure;

import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HuaweiAiGalleryChatProperties.CONFIG_PREFIX)
public class HuaweiAiGalleryChatProperties {

    public static final String CONFIG_PREFIX = "spring.ai.llm.chat";

    public static final String DEFAULT_CHAT_MODEL = "ernie-4.0-8k";

    private static final Float DEFAULT_TEMPERATURE = 0.95f;

    private static final Float DEFAULT_TOP_P = 1.0f;

    /**
     * Enable 百度千帆 chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever 百度千帆 options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private HuaweiAiGalleryChatOptions options = HuaweiAiGalleryChatOptions.builder()
            .withModel(DEFAULT_CHAT_MODEL)
            .withTemperature(DEFAULT_TEMPERATURE)
            .withTopP(DEFAULT_TOP_P)
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
