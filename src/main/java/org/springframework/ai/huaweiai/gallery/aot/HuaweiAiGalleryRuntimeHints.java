package org.springframework.ai.huaweiai.gallery.aot;

import org.springframework.ai.huaweiai.gallery.HuaweiAiGalleryChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class HuaweiAiGalleryRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(HuaweiAiGalleryChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
