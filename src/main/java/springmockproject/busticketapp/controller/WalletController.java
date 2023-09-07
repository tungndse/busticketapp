package springmockproject.busticketapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import springmockproject.busticketapp.entity.Wallet;
import springmockproject.busticketapp.login.Login;
import springmockproject.busticketapp.login.SessionUser;
import springmockproject.busticketapp.service.*;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class WalletController {

    private final UserService userService;
    private final BusService busService;
    private final LocationService locationService;
    private final TicketService ticketService;
    private final SeatService seatService;
    private final WalletService walletService;
    private final CheckoutService checkoutService;


    @Autowired
    public WalletController(BusService busService,
                            LocationService locationService,
                            TicketService ticketService,
                            SeatService seatService,
                            UserService userService,
                            WalletService walletService,
                            CheckoutService checkoutService) {


        this.busService = busService;
        this.locationService = locationService;
        this.ticketService = ticketService;
        this.seatService = seatService;
        this.userService = userService;
        this.walletService = walletService;
        this.checkoutService = checkoutService;
    }


    @GetMapping(value = "/customer/add_money")
    public String doAddMoney(HttpSession session, double options, Model model) {

        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");
        Wallet wallet = walletService.getWalletByUsername(sessionUser.getId());
        if (wallet == null) {
            return "redirect:/home";
        } else {
            long walletId = wallet.getId();
            walletService.addBalance(walletId, options);
            wallet = walletService.getWalletById(walletId);

            model.addAttribute("wallet", wallet);
        }

        log.info(String.valueOf(options));
        return "wallet";
    }

    @GetMapping(value = "/customer/my_wallet")
    public String openWallet(HttpSession session, Model model) {

        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");

        if (sessionUser == null) {
            model.addAttribute("cusViewWalletAction", "yes");
            model.addAttribute("login", new Login());
            return "login";
        }

        Wallet wallet = walletService.getWalletByUsername(sessionUser.getId());

        if (wallet != null) {
            model.addAttribute("wallet", wallet);
            return "wallet";
        } else {
            return "redirect:/home";
        }

    }




}
