package dev.ia;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookingService {
  private final Map<Long, Booking> bookings = new HashMap<>();

  public BookingService() {
    bookings.put(12345L, new Booking(12345L, "John Doe", "New York",
            LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1).plusDays(7), BookingStatus.CONFIRMED, Category.ADVENTURE));
    bookings.put(12346L, new Booking(12346L, "Jane Smith", "Los Angeles",
            LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(2).plusDays(8), BookingStatus.PENDING, Category.CITY));
  }

  public List<Booking> findPackagesByCategory(Category category) {
    return bookings.values().stream()
      .filter(booking -> booking.category().equals(category))
      .toList();
  }

  public Optional<Booking> getBookingDetails(Long bookingId) {
    return Optional.ofNullable(bookings.get(bookingId));
  }

  public List<Booking> getBookingsForCurrentUser() {
    String currentUser = SecurityContext.getCurrentUser();
    if (currentUser == null) {
      return List.of();
    }
    return bookings.values().stream()
      .filter(booking -> booking.customerName().equals(currentUser))
      .toList();
  }

  public Optional<Booking> cancelBooking(long bookingId) {
    String currentUser = SecurityContext.getCurrentUser();
    if (bookings.containsKey(bookingId)) {
      Booking booking = bookings.get(bookingId);
      if (booking.customerName().equals(currentUser)) {
        Booking cancelledBooking = new Booking(
          booking.id(),
          booking.customerName(),
          booking.destination(),
          booking.startDate(),
          booking.endDate(),
          BookingStatus.CANCELLED,
          booking.category()
        );
        this.bookings.replace(bookingId, cancelledBooking);
        return Optional.of(cancelledBooking);
      } 
    }
    return Optional.empty();
  }

}
