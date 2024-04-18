package org.springframework.ai.huaweiai.gallery.metadata;

import com.huaweicloud.pangu.dev.sdk.client.gallery.chat.GalleryChatResp;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class HuaweiAiGalleryChatResponseMetadata implements ChatResponseMetadata {

    public static HuaweiAiGalleryChatResponseMetadata from(GalleryChatResp response) {
        Assert.notNull(response, "Huawei GalleryChatResp must not be null");
        HuaweiAiGalleryUsage usage = HuaweiAiGalleryUsage.from(response.getUsage());
        HuaweiAiGalleryChatResponseMetadata chatResponseMetadata = new HuaweiAiGalleryChatResponseMetadata(response.getId(), usage);
        return chatResponseMetadata;
    }

    private final String id;
    private final Usage usage;

    public HuaweiAiGalleryChatResponseMetadata(String id, HuaweiAiGalleryUsage usage) {
        this.id = id;
        this.usage = usage;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Usage getUsage() {
        Usage usage = this.usage;
        return usage != null ? usage : new EmptyUsage();
    }

}
