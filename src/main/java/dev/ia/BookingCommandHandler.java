package dev.ia;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  BookingTools bookingTools;

  public Optional<String> tryHandle(String message) {
    if (message == null || message.isBlank()) {
      return Optional.empty();
    }

    Matcher cancelMatcher = CANCEL.matcher(message);
    if (cancelMatcher.matches()) {
      return Optional.of(bookingTools.cancelBooking(cancelMatcher.group(2)));
    }

    if (LIST.matcher(message).matches()) {
      return Optional.of(bookingTools.getMyBookings());
    }

    Matcher detailsMatcher = DETAILS.matcher(message);
    if (detailsMatcher.matches()) {
      return Optional.of(bookingTools.getBookingDetails(detailsMatcher.group(3)));
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
      case "cancelBooking" -> bookingTools.cancelBooking(matcher.group(2));
      case "getMyBookings" -> bookingTools.getMyBookings();
      case "getBookingDetails" -> bookingTools.getBookingDetails(matcher.group(2));
      default -> llmResponse;
    };
  }
}
