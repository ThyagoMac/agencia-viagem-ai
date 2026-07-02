package dev.ia;

import io.quarkiverse.langchain4j.RegisterAiService;

// Anotação para registrar o serviço
@RegisterAiService
public interface TravelAgentAssistant {
  // Método para receber a mensagem do usuário
  String chat(String userMessage);
}
