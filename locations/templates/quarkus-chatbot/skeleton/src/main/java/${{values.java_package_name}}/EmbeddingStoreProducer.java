package ${{ values.java_package_name }};

import jakarta.inject.Singleton;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class EmbeddingStoreProducer {

    @Singleton
    public InMemoryEmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
