package org.springframework.ai.huaweiai.gallery.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HuaweiAiGalleryIamProperties.CONFIG_PREFIX)
public class HuaweiAiGalleryIamProperties {

    public static final String CONFIG_PREFIX = "spring.ai.huaweiai.pangu.iam";

    private String url;
    private String domain;
    private String user;
    private String password;
    private String projectName;
    private Boolean disabled;
    private String ak;
    private String sk;
    @NestedConfigurationProperty
    private HuaweiAiGalleryHttpProxyProperties httpProxy;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public HuaweiAiGalleryHttpProxyProperties getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(HuaweiAiGalleryHttpProxyProperties httpProxy) {
        this.httpProxy = httpProxy;
    }
}
