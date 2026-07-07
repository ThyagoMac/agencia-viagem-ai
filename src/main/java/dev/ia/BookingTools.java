package dev.ia;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BookingTools {
  @Inject
  BookingService bookingService;

  @Tool("Obtém os detalhes de uma reserva com base em seu nume de identificação (bookingId).")
  public String getBookingDetails(Long bookingId) {
    return bookingService.getBookingDetails(bookingId)
      .map(booking -> booking.toString())
      .orElse("Reserva não encontrada");
  }

  @Tool("Cancela uma reserva com base em seu nume de identificação (bookingId) e no sobrenome do cliente (customerLastName).")
  public String cancelBooking(Long bookingId, String customerLastName) {
    return bookingService.cancelBooking(bookingId, customerLastName)
      .map(booking -> "Reserva " + booking.id() + " cancelada com sucesso. Status atual: " + booking.status())
      .orElse("Reserva não encontrada ou não pode ser cancelada. Verifique o bookingId e o sobrenome do cliente.");
  }
}
