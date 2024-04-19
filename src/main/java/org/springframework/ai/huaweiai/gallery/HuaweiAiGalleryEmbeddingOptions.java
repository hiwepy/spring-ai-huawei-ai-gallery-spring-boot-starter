package org.springframework.ai.huaweiai.gallery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.embedding.EmbeddingOptions;

import java.util.Map;

public class HuaweiAiGalleryEmbeddingOptions implements EmbeddingOptions {

    @JsonProperty("model")
    private String endpoint;

    @JsonProperty("model")
    private String appId;

    @JsonProperty("user")
    private String user;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected HuaweiAiGalleryEmbeddingOptions options;

        public Builder() {
            this.options = new HuaweiAiGalleryEmbeddingOptions();
        }

        public Builder withEndpoint(String endpoint) {
            this.options.setEndpoint(endpoint);
            return this;
        }

        public Builder withAppId(String appId) {
            this.options.setAppId(appId);
            return this;
        }

        public Builder withUser(String user) {
            this.options.setUser(user);
            return this;
        }

        public HuaweiAiGalleryEmbeddingOptions build() {
            return this.options;
        }

    }

    /**
     * Convert the {@link HuaweiAiGalleryEmbeddingOptions} object to a {@link Map} of key/value pairs.
     * @return The {@link Map} of key/value pairs.
     */
    public Map<String, Object> toMap() {
        try {
            var json = new ObjectMapper().writeValueAsString(this);
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
