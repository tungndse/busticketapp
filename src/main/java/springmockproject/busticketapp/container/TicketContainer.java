package springmockproject.busticketapp.container;

import lombok.Data;
import springmockproject.busticketapp.entity.Seat;
import springmockproject.busticketapp.entity.Ticket;
import springmockproject.busticketapp.entity.User;

import java.util.List;

@Data
public class TicketContainer {

    private Seat seat;

    private List<Ticket> tickets;

    private User bookingUser;

    private Ticket bookingTicket;

    public TicketContainer(Seat seat, List<Ticket> tickets) {
        this.seat = seat;
        this.tickets = tickets;
    }

    public User getBookingUser() {
        if (!seat.isBooked() || tickets.isEmpty())
            return null;

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == 1) {
                return ticket.getUser();
            }
        }

        return null;
    }

    public Ticket getBookingTicket() {

        if (!seat.isBooked() || tickets.isEmpty())
            return null;

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == 1) {
                return ticket;
            }
        }

        return null;
    }
}
