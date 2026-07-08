package dev.ia;

import java.util.List;
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

  @Tool("Lista as reservas já feitas pelo usuário autenticado. Não use para o catálogo de pacotes disponíveis.")
  public String getMyBookings() {
    List<Booking> bookings = bookingService.getBookingsForCurrentUser();
    if (bookings.isEmpty()) {
      return "Nenhuma reserva encontrada para o usuário autenticado.";
    }
    return bookings.stream()
      .map(BookingTools::formatBooking)
      .reduce((a, b) -> a + "\n" + b)
      .orElse("");
  }

  @Tool("Cancela uma reserva pelo bookingId. O usuário já está autenticado; basta o número da reserva.")
  public String cancelBooking(
    @P("Número da reserva, apenas dígitos (ex: 12345)") String bookingId
  ) {
    return parseBookingId(bookingId)
      .flatMap(id -> bookingService.cancelBooking(id))
      .map(booking -> "Reserva " + booking.id() + " cancelada com sucesso. Status atual: " + booking.status())
      .orElse("Reserva não encontrada ou não pertence ao usuário autenticado. Verifique o bookingId.");
  }

  private static String formatBooking(Booking booking) {
    return "Reserva %d: %s (%s a %s) - %s [%s]".formatted(
      booking.id(),
      booking.destination(),
      booking.startDate(),
      booking.endDate(),
      booking.status(),
      booking.category()
    );
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
