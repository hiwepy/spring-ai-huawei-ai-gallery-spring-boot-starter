package org.springframework.ai.huaweiai.gallery.metadata;

import com.huaweicloud.pangu.dev.sdk.client.gallery.chat.GalleryTokenUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class HuaweiAiGalleryUsage implements Usage {

    public static HuaweiAiGalleryUsage from(GalleryTokenUsage usage) {
        return new HuaweiAiGalleryUsage(usage);
    }

    private final GalleryTokenUsage usage;

    protected HuaweiAiGalleryUsage(GalleryTokenUsage usage) {
        Assert.notNull(usage, "Huawei AI GalleryUsage must not be null");
        this.usage = usage;
    }

    protected GalleryTokenUsage getUsage() {
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
