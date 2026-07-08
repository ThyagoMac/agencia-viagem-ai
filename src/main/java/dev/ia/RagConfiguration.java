package dev.ia;

import java.util.function.Supplier;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RagConfiguration implements Supplier<RetrievalAugmentor> {

  private final RetrievalAugmentor augmentor;

  @Inject
  public RagConfiguration(
    EmbeddingStore<TextSegment> embeddingStore,
    EmbeddingModel embeddingModel
  ) {
    var contentRetriever = EmbeddingStoreContentRetriever.builder()
      .embeddingStore(embeddingStore)
      .embeddingModel(embeddingModel)
      .maxResults(5)
      .build();

    augmentor = DefaultRetrievalAugmentor.builder()
      .contentRetriever(contentRetriever)
      .contentInjector(catalogContentInjector())
      .build();
  }

  private static ContentInjector catalogContentInjector() {
    return (contents, userMessage) -> {
      String query = userMessage instanceof UserMessage user
        ? user.singleText()
        : userMessage.toString();
      StringBuilder prompt = new StringBuilder(query);
      if (!contents.isEmpty()) {
        prompt.append("\n\nCatálogo de pacotes (use SOMENTE estas informações para recomendar pacotes):");
        for (Content content : contents) {
          prompt.append("\n").append(content.textSegment().text());
        }
      }
      return UserMessage.from(prompt.toString());
    };
  }

  @Override
  public RetrievalAugmentor get() {
    return augmentor;
  }
}
