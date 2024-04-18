package org.springframework.ai.huaweiai.gallery.metadata;

import com.huaweicloud.pangu.dev.sdk.client.gallery.GalleryUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class HuaweiAiGalleryUsage implements Usage {

    public static HuaweiAiGalleryUsage from(GalleryUsage usage) {
        return new HuaweiAiGalleryUsage(usage);
    }

    private final GalleryUsage usage;

    protected HuaweiAiGalleryUsage(GalleryUsage usage) {
        Assert.notNull(usage, "Huawei AI GalleryUsage must not be null");
        this.usage = usage;
    }

    protected GalleryUsage getUsage() {
        return this.usage;
    }

    @Override
    public Long getPromptTokens() {
        return getUsage().getPromptTokens();
    }

    @Override
    public Long getGenerationTokens() {
        return getUsage().getCompletionTokens();
    }

    @Override
    public Long getTotalTokens() {
        return getUsage().getTotalTokens();
    }

    @Override
    public String toString() {
        return getUsage().toString();
    }

}
