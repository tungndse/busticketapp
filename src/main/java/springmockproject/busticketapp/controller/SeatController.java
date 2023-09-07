package springmockproject.busticketapp.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import springmockproject.busticketapp.entity.Bus;
import springmockproject.busticketapp.entity.Checkout;
import springmockproject.busticketapp.entity.Seat;
import springmockproject.busticketapp.login.SessionUser;
import springmockproject.busticketapp.repository.BusRepository;
import springmockproject.busticketapp.repository.SeatRepository;
import springmockproject.busticketapp.search.BusSearchInfo;
import springmockproject.busticketapp.service.BusService;
import springmockproject.busticketapp.service.CheckoutService;
import springmockproject.busticketapp.service.SeatService;
import springmockproject.busticketapp.service.UserService;
import springmockproject.busticketapp.utils.SeatUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class SeatController {
    private final BusService busService;
    private final SeatService seatService;
    private final UserService userService;
    private final CheckoutService checkoutService;

    @Autowired
    public SeatController(BusService busService, SeatService seatService,
                          UserService userService, CheckoutService checkoutService) {
        this.busService = busService;
        this.seatService = seatService;
        this.userService = userService;
        this.checkoutService = checkoutService;
    }

    @GetMapping(value = "/customer/view_selected_bus")
    public String showAvailableSeats(Model model, String busId, HttpSession session,
                                     HttpServletRequest request) {
        Bus bus = busService.getBusById(busId);

        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");
        if (sessionUser != null) {
            log.info(sessionUser.getId());
            log.info(sessionUser.getFirstName());

            Checkout draftCheckout = checkoutService
                    .getOngoingCheckoutForBooker
                            (sessionUser.getId(), busId);

            if (draftCheckout != null) {
                request.setAttribute("busId", busId);
                request.setAttribute("draftCheckoutId", draftCheckout.getId());

                return "forward:/customer/book_seats_get";
            }
        }

        List<List<Seat>> matrixList = seatService.getSeatsMatrixOfBus(bus);
        model.addAttribute("seatsMatrix", matrixList);
        //For infomation of the bus on seats page - 04/08/2020
        model.addAttribute("bus", bus);
        return "view-seats";


    }

    @GetMapping(value = "/conductor/view_selected_bus")
    public String showAvailableSeats2(Model model, String busId) {
        Bus bus = busService.getBusById(busId);
        List<List<Seat>> matrixList = seatService.getSeatsMatrixOfBus(bus);
        model.addAttribute("seatsMatrix", matrixList);
        model.addAttribute("currentBus", bus);
        return "conductor-view-seats";
    }


}
