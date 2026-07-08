package dev.ia;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/travel")
public class TravelAgentResource {
  @Inject
  PackageExpert expert;

  @Inject
  BookingCommandHandler bookingCommandHandler;

  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public String ask(String question, @HeaderParam("X-User-Name") String userName) {
    if (userName != null && !userName.isEmpty()) {
      try {
        SecurityContext.setCurrentUser(userName);
        return bookingCommandHandler.tryHandle(question)
          .orElseGet(() -> bookingCommandHandler.resolveResponse(expert.chat(userName, question)));
      } catch (Exception e) {
        return "Error: " + e.getMessage() + " Please provide a valid user name";
      } finally {
        SecurityContext.clear();
      }
    }
    return "Please provide a valid user name";
  }
}
