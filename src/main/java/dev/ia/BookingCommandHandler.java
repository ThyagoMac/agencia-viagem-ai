package dev.ia;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BookingCommandHandler {
  private static final Pattern CANCEL = Pattern.compile(
    "(?i).*\\b(cancela|cancelar|cancele)\\b.*?(\\d{4,})"
  );
  private static final Pattern LIST = Pattern.compile(
    "(?i).*(minhas?\\s+(viagens|reservas)|viagens?\\s+tenho|reservas?\\s+tenho|quais\\s+(viagens|reservas)|tenho\\s+em\\s+meu\\s+nome).*"
  );
  private static final Pattern DETAILS = Pattern.compile(
    "(?i).*(detalhes?|informa(ç|c)(õ|o)es?).*?(\\d{4,})"
  );
  private static final Pattern TOOL_JSON = Pattern.compile(
    "\"name\"\\s*:\\s*\"(cancelBooking|getMyBookings|getBookingDetails)\"(?:.*\"bookingId\"\\s*:\\s*\"?(\\d+)\"?)?",
    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
  );

  @Inject
  @McpClientName("booking-server")
  McpClient bookingClient;

  public Optional<String> tryHandle(String message) {
    if (message == null || message.isBlank()) {
      return Optional.empty();
    }

    Matcher cancelMatcher = CANCEL.matcher(message);
    if (cancelMatcher.matches()) {
      return Optional.of(callTool("cancelBooking", cancelMatcher.group(2)));
    }

    if (LIST.matcher(message).matches()) {
      return Optional.of(callTool("getMyBookings", null));
    }

    Matcher detailsMatcher = DETAILS.matcher(message);
    if (detailsMatcher.matches()) {
      return Optional.of(callTool("getBookingDetails", detailsMatcher.group(3)));
    }

    return Optional.empty();
  }

  public String resolveResponse(String llmResponse) {
    if (llmResponse == null || llmResponse.isBlank()) {
      return llmResponse;
    }

    Matcher matcher = TOOL_JSON.matcher(llmResponse);
    if (!matcher.find()) {
      return llmResponse;
    }

    return switch (matcher.group(1)) {
      case "cancelBooking" -> callTool("cancelBooking", matcher.group(2));
      case "getMyBookings" -> callTool("getMyBookings", null);
      case "getBookingDetails" -> callTool("getBookingDetails", matcher.group(2));
      default -> llmResponse;
    };
  }

  private String callTool(String toolName, String bookingId) {
    String userName = SecurityContext.getCurrentUser();
    StringBuilder arguments = new StringBuilder("{");
    if (userName != null && !userName.isBlank()) {
      arguments.append("\"userName\":\"").append(escapeJson(userName)).append("\"");
    }
    if (bookingId != null) {
      if (arguments.length() > 1) {
        arguments.append(',');
      }
      arguments.append("\"bookingId\":\"").append(bookingId).append("\"");
    }
    arguments.append('}');

    ToolExecutionRequest request = ToolExecutionRequest.builder()
      .name(toolName)
      .arguments(arguments.toString())
      .build();

    ToolExecutionResult result = bookingClient.executeTool(request);
    return result.resultText();
  }

  private static String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
