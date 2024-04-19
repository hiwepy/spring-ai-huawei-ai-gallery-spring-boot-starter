package org.springframework.ai.huaweiai.gallery.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HuaweiAiGalleryConnectionProperties.CONFIG_PREFIX)
public class HuaweiAiGalleryConnectionProperties extends HuaweiAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.huaweiai.pangu";

    private String moduleVersion;
    private String systemPrompt;
    private boolean enableAppendSystemMessage;

    @NestedConfigurationProperty
    private HuaweiAiGalleryHttpProxyProperties httpProxy;

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public boolean isEnableAppendSystemMessage() {
        return enableAppendSystemMessage;
    }

    public void setEnableAppendSystemMessage(boolean enableAppendSystemMessage) {
        this.enableAppendSystemMessage = enableAppendSystemMessage;
    }

    public HuaweiAiGalleryHttpProxyProperties getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(HuaweiAiGalleryHttpProxyProperties httpProxy) {
        this.httpProxy = httpProxy;
    }

}
