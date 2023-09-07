package springmockproject.busticketapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import springmockproject.busticketapp.entity.Bus;
import springmockproject.busticketapp.entity.Seat;
import springmockproject.busticketapp.entity.Ticket;
import springmockproject.busticketapp.entity.User;
import springmockproject.busticketapp.exception.BookingException;
import springmockproject.busticketapp.repository.BusRepository;
import springmockproject.busticketapp.repository.SeatRepository;
import springmockproject.busticketapp.repository.TicketRepository;
import springmockproject.busticketapp.repository.UserRepository;
import springmockproject.busticketapp.utils.SeatUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SeatService {


    private final UserRepository userRepo;

    private final TicketRepository ticketRepo;

    private final SeatRepository seatRepo;

    private final BusRepository busRepo;

    private final TicketService ticketService;


    @Autowired
    public SeatService(TicketRepository ticketRepo,
                       SeatRepository seatRepo,
                       UserRepository userRepo,
                       BusRepository busRepo,
                       TicketService ticketService) {
        this.ticketRepo = ticketRepo;
        this.seatRepo = seatRepo;
        this.userRepo = userRepo;
        this.busRepo = busRepo;
        this.ticketService = ticketService;

    }

    @Transactional(rollbackFor = BookingException.class)
    public void bookSeatsByIds(User user, String[] seatIds) throws BookingException {

        for (String seatId : seatIds) {
            bookSeatById(user, seatId);
        }
    }

    public void bookSeatById(User user, String seatId)
            throws BookingException {
        Seat seat = seatRepo.getById(seatId);

        boolean isBooked = seat.getTicketContainer()
                .getBookingTicket() != null;

        if (!isBooked) {

            /*Ticket ticket = new Ticket();
            ticket.setUser(user);
            ticket.setSeat(seat);*/
            Ticket ticket = Ticket.builder()
                    .user(user)
                    .seat(seat)
                    .status(1)
                    .build();

            //log.info(ticket != null ? "Not nuyll" : " null");
            seatRepo.changeSeatStatus(true, seat.getId());

            ticketRepo.save(ticket);

        } else {
            //Need more tuning
            throw new BookingException();
        }
    }

    public boolean cancelSeats(String[] seatIds) {
        for (String seatId : seatIds) {
            Seat seat = seatRepo.getById(seatId);

            Ticket bookingTicket = seat.getTicketContainer().getBookingTicket();

            try {
                ticketService.cancelTicketByIdInStringType
                        (String.valueOf(bookingTicket.getId()));
            } catch (BookingException be) {
                log.info(be.getMessage());
                return false;
            }

        }

        return true;
    }

    public List<List<Seat>> getSeatsMatrixOfBus(Bus bus) {
        List<Seat> seatList = bus.getSeats();
        return SeatUtils.convertToMatrix(seatList);
    }


    public Seat getSeatById(String seatId) {
        return seatRepo.getById(seatId);
    }

    public List<Seat> getSeatByIds(String[] seatIds) {
        List<Seat> list = new ArrayList<>();

        for (String seatId : seatIds) {
            Seat seat = seatRepo.getById(seatId);
            if (seat.isBooked()) {
                return null;
            }

            list.add(seat);
        }

        if (list.size() > 0)
            return list;
        else
            return null;
    }
}
