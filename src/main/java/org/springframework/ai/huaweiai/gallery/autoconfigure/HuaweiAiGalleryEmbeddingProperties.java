package org.springframework.ai.huaweiai.gallery.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HuaweiAiGalleryEmbeddingProperties.CONFIG_PREFIX)
public class HuaweiAiGalleryEmbeddingProperties {

    public static final String CONFIG_PREFIX = "spring.ai.huaweiai.gallery.embedding";

    public static final String DEFAULT_EMBEDDING_MODEL = "embedding-v1";

    /**
     * Enable Huawei Gallery embedding client.
     */
    private boolean enabled = true;

    public MetadataMode metadataMode = MetadataMode.EMBED;

    /**
     * Client lever Huawei Gallery options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private HuaweiAiGalleryEmbeddingOptions options = HuaweiAiGalleryEmbeddingOptions.builder()
            .withModel(DEFAULT_EMBEDDING_MODEL)
            .build();

    public HuaweiAiGalleryEmbeddingOptions getOptions() {
        return this.options;
    }

    public void setOptions(HuaweiAiGalleryEmbeddingOptions options) {
        this.options = options;
    }

    public MetadataMode getMetadataMode() {
        return this.metadataMode;
    }

    public void setMetadataMode(MetadataMode metadataMode) {
        this.metadataMode = metadataMode;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
