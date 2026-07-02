package dev.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(retrievalAugmentor = RagConfiguration.class)
public interface PackageExpert {
  @SystemMessage("""
      Você é um especialista em pacotes de viagem.
      Use o contexto fornecido para recomendar pacotes adequados.
      Responda sempre em português do Brasil, de forma clara e objetiva (no máximo 3 parágrafos).
      """)
    String chat(@MemoryId String memoryId, String userMessage);
}
