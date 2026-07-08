package dev.ia;

import java.util.Optional;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BookingTools {
  @Inject
  BookingService bookingService;

  @Tool("Obtém os detalhes de uma reserva com base em seu número de identificação (bookingId).")
  public String getBookingDetails(@P("Número da reserva, apenas dígitos (ex: 12345)") String bookingId) {
    return parseBookingId(bookingId)
      .flatMap(bookingService::getBookingDetails)
      .map(Booking::toString)
      .orElse("Reserva não encontrada");
  }

  @Tool("Cancela uma reserva com base em seu número de identificação (bookingId) e no sobrenome do cliente (customerLastName).")
  public String cancelBooking(
    @P("Número da reserva, apenas dígitos (ex: 12345)") String bookingId,
    @P("Sobrenome do cliente (ex: Doe)") String customerLastName
  ) {
    return parseBookingId(bookingId)
      .flatMap(id -> bookingService.cancelBooking(id, customerLastName))
      .map(booking -> "Reserva " + booking.id() + " cancelada com sucesso. Status atual: " + booking.status())
      .orElse("Reserva não encontrada ou não pode ser cancelada. Verifique o bookingId e o sobrenome do cliente.");
  }

  private static Optional<Long> parseBookingId(String bookingId) {
    if (bookingId == null || bookingId.isBlank()) {
      return Optional.empty();
    }
    String digits = bookingId.replaceAll("\\D", "");
    if (digits.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(Long.parseLong(digits));
  }
}
