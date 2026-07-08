package dev.ia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class DocumentIngestor {
  private static final Pattern CATEGORY = Pattern.compile("Categoria:\\s*(\\w+)", Pattern.CASE_INSENSITIVE);

  @Inject
  EmbeddingStore<TextSegment> store;

  @Inject
  EmbeddingModel embeddingModel;

  public void onStart(@Observes StartupEvent event) {
    String content = loadResource("/rag/pacotes-viagem.md");
    String[] sections = content.split("(?=### )");

    EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
      .embeddingModel(embeddingModel)
      .embeddingStore(store)
      .build();

    for (String section : sections) {
      if (section.isBlank()) {
        continue;
      }

      Document document = Document.from(section.trim());
      document.metadata().put("type", "package");
      extractCategory(section).ifPresent(category -> document.metadata().put("category", category));
      ingestor.ingest(document);
    }
  }

  private String loadResource(String path) {
    try (InputStream input = getClass().getResourceAsStream(path)) {
      if (input == null) {
        throw new IllegalStateException("Arquivo RAG não encontrado: " + path);
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Falha ao carregar " + path, e);
    }
  }

  private static Optional<String> extractCategory(String section) {
    Matcher matcher = CATEGORY.matcher(section);
    if (matcher.find()) {
      return Optional.of(matcher.group(1).toUpperCase());
    }
    return Optional.empty();
  }
}
