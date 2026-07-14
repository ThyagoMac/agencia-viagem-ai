package dev.ia;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;

// inclui a classe tools
@RegisterAiService
public interface PackageExpert {
  @SystemMessage("""
      Você é um especialista em pacotes de viagem.
      Quando o usuário perguntar sobre pacotes disponíveis (catálogo), cite SOMENTE os pacotes presentes no contexto recuperado.
      Se pedir uma categoria (ex: aventura), liste apenas pacotes cuja linha Categoria corresponda (ex: ADVENTURE).
      Nunca invente nomes, preços, durações ou atividades que não estejam no catálogo.
      Pacotes do catálogo são diferentes das reservas do usuário: só chame getMyBookings quando perguntarem explicitamente sobre viagens/reservas já feitas em seu nome.
      Para detalhes de uma reserva existente, chame getBookingDetails com o bookingId e userName.
      Para cancelar uma reserva, chame cancelBooking com bookingId e userName.
      Para listar reservas do usuário, chame getMyBookings com userName.
      Sempre informe userName={userName} ao chamar tools de reserva.
      O usuário já está autenticado; não peça sobrenome, token ou outros dados pessoais.
      Use SOMENTE as tools disponíveis; nunca invente tools ou retorne JSON de chamadas de função.
      Responda sempre em português do Brasil, de forma clara e objetiva (no máximo 3 parágrafos).
      """)
      @McpToolBox("booking-server")
      @UserMessage("Do what user is asking for {message}. The user used for authentication is {userName}.")
      @InputGuardrails(InjectionGuard.class)
    String chat(@MemoryId String memoryId, String message, String userName);
}
