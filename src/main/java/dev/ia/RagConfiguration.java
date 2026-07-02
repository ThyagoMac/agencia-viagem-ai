package dev.ia;

import java.util.function.Supplier;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RagConfiguration implements Supplier<RetrievalAugmentor> {
  @Inject
  EmbeddingStore<TextSegment> embeddingStore;

  @Inject
  EmbeddingModel embeddingModel;

  @Override
  public RetrievalAugmentor get() {
    return DefaultRetrievalAugmentor.builder()
      .contentRetriever(EmbeddingStoreContentRetriever.builder()
        .embeddingStore(embeddingStore)
        .embeddingModel(embeddingModel)
        .maxResults(5)
        .build())
      .build();
  }
}
