package springmockproject.busticketapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import springmockproject.busticketapp.entity.*;
import springmockproject.busticketapp.exception.BookingException;
import springmockproject.busticketapp.login.Login;
import springmockproject.busticketapp.login.SessionUser;
import springmockproject.busticketapp.search.BusSearchInfo;
import springmockproject.busticketapp.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
public class TicketController {

    private final UserService userService;
    private final BusService busService;
    private final LocationService locationService;
    private final TicketService ticketService;
    private final SeatService seatService;
    private final CheckoutService checkoutService;


    @Autowired
    public TicketController(BusService busService,
                            LocationService locationService,
                            TicketService ticketService,
                            SeatService seatService,
                            UserService userService,
                            CheckoutService checkoutService) {


        this.busService = busService;
        this.locationService = locationService;
        this.ticketService = ticketService;
        this.seatService = seatService;
        this.userService = userService;
        this.checkoutService = checkoutService;
    }

    @GetMapping(value = "/customer/book_seats_get")
    public String preCheckoutGet(String busId,
                                 HttpSession session,
                                 String draftCheckoutId, Model model) {

        log.info(busId != null ? busId : "null bus id");

        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");

        User booker = userService.searchById(sessionUser.getId());
        Bus targetBus = busService.getBusById(busId);

        Checkout draft = null;
        if (draftCheckoutId == null) {
            draft = checkoutService.getOngoingCheckoutForBooker
                    (booker.getId(),
                            targetBus.getId());
        } else {
            draft = checkoutService.getById(draftCheckoutId);
        }

        if (draft != null) {
            log.info("found drafT!!!");

            model.addAttribute("checkoutSeats", draft.getCheckoutSeats());
            model.addAttribute("booker", draft.getBooker());
            model.addAttribute("bus", draft.getBus());
            return "checkout";

        } else {
            return "redirect:/customer/home";
        }
    }

    @PostMapping(value = "/customer/book_seats")
    public String preCheckout(HttpSession session,
                              String[] seatIds,
                              String errorBooking,
                              Model model,
                              HttpServletRequest request) {

        log.info("PreCheckout");

        log.info(seatIds != null ? "seats id ok" : "null seat id");
        for (String seatid : seatIds) {
            log.info(seatid);
        }

        if (seatIds != null && seatIds.length > 0) {
            log.info("gettting session user");
            SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");
            if (sessionUser == null) {
                log.info("NULL USER");
                model.addAttribute("login", new Login());
                model.addAttribute("seatIds", seatIds);
                return "login";
            }


            log.info(sessionUser.getId());

            log.info("Login success");

            //TODO NEW FEATURE COMES HERE
            User booker = userService.searchById(sessionUser.getId());
            Bus targetBus = busService.getBusById
                    (seatService
                            .getSeatById(seatIds[0])
                            .getBus()
                            .getId());

            Checkout draft = checkoutService.getOngoingCheckoutForBooker
                    (booker.getId(), targetBus.getId());

            if (draft != null) {
                log.info("found drafT!!!");

                model.addAttribute("checkoutSeats", draft.getCheckoutSeats());
                model.addAttribute("booker", draft.getBooker());
                model.addAttribute("bus", draft.getBus());
                return "checkout";

            }

            log.info("found no DRAFT");

            List<Seat> seats = seatService.getSeatByIds(seatIds);

            if (seats == null) {
                model.addAttribute("error_choosing_seats",
                        "Sorry, one or more of your chosen seats have " +
                                "already been booked, please choose again");

                request.setAttribute("busId", targetBus.getId());

                return "forward:/customer/view_selected_bus";
            }

            if (errorBooking != null) {
                model.addAttribute("errorBooking", errorBooking);
            }


            model.addAttribute("seats", seats);
            model.addAttribute("booker", booker);
            model.addAttribute("bus", targetBus);


            return "checkout";


            //------------------------------


        }

        return "redirect:/customer/view_my_tickets";
    }

    @GetMapping(value = "/customer/book_seats/cancel_payment")
    public String cancelCheckout(String draftId, String busId,
                                 HttpServletRequest request) {

        try {
            if (draftId != null) {
                checkoutService.cancelDraft(draftId);
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }

        request.setAttribute("busId", busId);
        return "forward:/customer/view_selected_bus";
    }


    @PostMapping(value = "/customer/book_seats/confirm_payment")
    @ExceptionHandler(BookingException.class)
    public String postCheckout(HttpSession session,
                               String draftId,
                               String bookerId,
                               String[] seatIds,
                               String[] passengerNames,
                               String[] passengerPIDs,
                               String[] childSeats,
                               HttpServletRequest request) {

        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");

        if (sessionUser != null
                && sessionUser.getId().equals(bookerId)
                && userService.searchById(sessionUser.getId()).getRole() == 0) {

            Checkout draft = null;
            try {
                if (draftId != null) {
                    long draftIdInt = checkoutService.updateDraft(draftId, seatIds, passengerNames, passengerPIDs, childSeats);
                    draft = checkoutService.getById(draftIdInt);
                } else {
                    long draftIdInt = checkoutService.makingDraft(bookerId, seatIds, passengerNames, passengerPIDs, childSeats);
                    draft = checkoutService.getById(draftIdInt);
                }

                ticketService.bookTickets(bookerId, seatIds, passengerNames, passengerPIDs, childSeats);
            } catch (BookingException be) {
                String msg = null;
                if (be.getMessage().contains("CustomerException-")) {
                    msg = be.getMessage().replace("CustomerException-", "");
                }

                request.setAttribute("errorBooking", msg);
                request.setAttribute("seatIds", seatIds);

                if (draft != null) {
                    request.setAttribute("draftCheckout", draft);
                }

                return "forward:/customer/book_seats";
            }

            if (draft != null) {
                ticketService.setDraftDone(draft);
            }

            return "redirect:/customer/view_my_tickets";
        } else {
            return "redirect:/logout";
        }

    }

    @GetMapping(value = "/customer/view_my_tickets")
    public String viewCustomerTickets(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");

        if (sessionUser == null) {
            model.addAttribute("cusViewTicketsAction", "yes");
            model.addAttribute("login", new Login());
            return "login";
        }

        String userId = sessionUser.getId();

        User user = userService.searchById(userId);

        List<Ticket> list = ticketService.getTicketListByUserAndStatus(user, 1);

        model.addAttribute("userTickets", list);
        return "view-tickets";
    }

    @GetMapping(value = "/conductor/cancel_seats")
    public String cancelSeats(String[] seatIds, String busId, Model model) throws InterruptedException {

        boolean check = false;
        if (seatIds != null) {
            check = seatService.cancelSeats(seatIds);
        }


        return "redirect:/conductor/search_bus";

    }


    @GetMapping(value = "/cancel_ticket")
    public String cancelTicket(String ticketIdStr) {
        try {
            ticketService.cancelTicketByIdInStringType(ticketIdStr);
        } catch (BookingException be) {
            log.info(be.getMessage());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return "redirect:/customer/view_my_tickets";
    }

    @GetMapping(value = "/conductor-view-all-tickets-booked")
    public String viewAllTicketsBooked(Model model) {
        return "conductor-search-tickets";
    }

    @GetMapping(value = "/conductor/searchTickets")
    public String searchTickets1(String statusStr, Model model) {
        List<Ticket> list = ticketService.getTicketListByStatusString(statusStr);
        if (list != null && !list.isEmpty()) {
            model.addAttribute("listTickets", list);
        } else {
            model.addAttribute("noTicketFoundMsg", "Found no tickets");
        }
        return "conductor-view-tickets";
    }

    @GetMapping(value = "/conductor/search_tickets_by_user/commit")
    public String searchTickets(String userId, String searchMethod,
                                String ticketStatus,
                                Model model) {

        if (searchMethod.equals("Search By User")) {
            User user = userService.searchById(userId);
            List<Ticket> list = ticketService.getTicketListByUserAndStatus(user, ticketStatus);

            if (list != null && !list.isEmpty()) {
                model.addAttribute("listTickets", list);
            } else {
                model.addAttribute("noTicketFoundMsg", "Found no tickets");
            }

            return "conductor-search-tickets";
        } else {
            List<Location> listLoc = locationService.getAllLocations();
            model.addAttribute("listLoc", listLoc);
            model.addAttribute("searchInfo", new BusSearchInfo());
            model.addAttribute("ticketStatus", ticketStatus);
            return "conductor-search-bus-for-tickets";
        }

    }

    @GetMapping(value = "/conductor/view_tickets_of_selected_bus")
    public String searchTicketsByBus(String busId, String ticketStatus, Model model) {

        log.info("Ticket status on Ticket controller: " + ticketStatus);

        List<Ticket> list = ticketService.getTicketListByBusIdAndStatus(busId, ticketStatus);

        model.addAttribute("listTickets", list);

        return "conductor-search-tickets";

    }

    @GetMapping(value = "/conductor/cancel_tickets")
    public String cancelTickets(String[] ticketIds) {
        try {
            if (ticketIds != null && ticketIds.length > 0) {
                ticketService.cancelTickets(ticketIds);
            }
        } catch (BookingException be) {
            log.info(be.getMessage());
        }

        return "conductor-search-tickets";

    }


}
