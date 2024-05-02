package ${{ values.java_package_name }};

import java.util.List;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RetrieverExample implements Retriever<TextSegment> {

    private final EmbeddingStoreRetriever retriever;

    RetrieverExample(EmbeddingStore<TextSegment> store, EmbeddingModel model) {
        retriever = EmbeddingStoreRetriever.from(store, model, 10);
    }

    @Override
    public List<TextSegment> findRelevant(String s) {
        return retriever.findRelevant(s);
    }
}
