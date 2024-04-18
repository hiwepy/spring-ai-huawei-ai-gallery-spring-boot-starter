package org.springframework.ai.huaweiai.gallery;

import com.huaweicloud.pangu.dev.sdk.client.gallery.GalleryClient;
import com.huaweicloud.pangu.dev.sdk.client.gallery.GalleryUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HuaweiAiGalleryEmbeddingClient extends AbstractEmbeddingClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HuaweiAiGalleryEmbeddingOptions defaultOptions;

    private final MetadataMode metadataMode;

    /**
     * Low-level Huawei Gallery API library.
     */
    private final GalleryClient galleryClient;

    private final RetryTemplate retryTemplate;

    public HuaweiAiGalleryEmbeddingClient(GalleryClient galleryClient) {
        this(galleryClient, MetadataMode.EMBED);
    }

    public HuaweiAiGalleryEmbeddingClient(GalleryClient galleryClient, MetadataMode metadataMode) {
        this(galleryClient, metadataMode, HuaweiAiGalleryEmbeddingOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiGalleryEmbeddingClient(GalleryClient galleryClient, MetadataMode metadataMode, HuaweiAiGalleryEmbeddingOptions options) {
        this(galleryClient, metadataMode, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiGalleryEmbeddingClient(GalleryClient galleryClient, MetadataMode metadataMode, HuaweiAiGalleryEmbeddingOptions options, RetryTemplate retryTemplate) {
        Assert.notNull(galleryClient, "galleryClient must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");

        this.galleryClient = galleryClient;
        this.metadataMode = metadataMode;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public List<Double> embed(Document document) {
        Assert.notNull(document, "Document must not be null");
        return this.embed(document.getFormattedContent(this.metadataMode));
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return this.retryTemplate.execute(ctx -> {
            logger.debug("Retrieving embeddings");

            Assert.notEmpty(request.getInstructions(), "At least one text is required!");
            if (request.getInstructions().size() != 1) {
                logger.warn( "Huawei AI Embedding does not support batch embedding. Will make multiple API calls to embed(Document)");
            }

            var apiRequest = toEmbeddingRequest(request);
            GalleryEmbeddingResp apiEmbeddingResponse = galleryClient.createEmbeddings(apiRequest);
            if (Objects.isNull(apiEmbeddingResponse) || CollectionUtils.isEmpty(apiEmbeddingResponse.getData())){
                logger.warn("No embeddings returned for request: {}", request);
                return new EmbeddingResponse(List.of());
            }
            logger.debug("Embeddings retrieved");
            return generateEmbeddingResponse(apiEmbeddingResponse);

        });
    }

    private GalleryEmbeddingReq toEmbeddingRequest(EmbeddingRequest request) {
        var galleryEmbeddingReq = (this.defaultOptions != null)
                ? GalleryEmbeddingReq.builder().input(request.getInstructions()).user(this.defaultOptions.getUser()).build()
                : GalleryEmbeddingReq.builder().input(request.getInstructions()).build();

        if (request.getOptions() != null && !EmbeddingOptions.EMPTY.equals(request.getOptions())) {
            galleryEmbeddingReq = ModelOptionsUtils.merge(request.getOptions(), galleryEmbeddingReq, GalleryEmbeddingReq.class);
        }
        return galleryEmbeddingReq;
    }

    private EmbeddingResponse generateEmbeddingResponse(GalleryEmbeddingResp embeddingResponse) {
        List<Embedding> data = generateEmbeddingList(embeddingResponse.getData());
        EmbeddingResponseMetadata metadata = generateMetadata(embeddingResponse.getUsage());
        return new EmbeddingResponse(data, metadata);
    }

    private List<Embedding> generateEmbeddingList(List<GalleryEmbedding> nativeData) {
        List<Embedding> data = new ArrayList<>();
        for (GalleryEmbedding nativeDatum : nativeData) {
            List<Float> nativeDatumEmbedding = nativeDatum.getEmbedding();
            int nativeIndex = nativeDatum.getIndex();
            Embedding embedding = new Embedding(nativeDatumEmbedding.stream().map(Float::doubleValue).collect(Collectors.toList()), nativeIndex);
            data.add(embedding);
        }
        return data;
    }

    private EmbeddingResponseMetadata generateMetadata(GalleryUsage embeddingsUsage) {
        EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
        // metadata.put("model", model);
        metadata.put("prompt-tokens", embeddingsUsage.getPromptTokens());
        metadata.put("completion-tokens", embeddingsUsage.getCompletionTokens());
        metadata.put("total-tokens", embeddingsUsage.getTotalTokens());
        return metadata;
    }

}
