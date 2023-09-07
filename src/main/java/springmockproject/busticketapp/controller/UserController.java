package springmockproject.busticketapp.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springmockproject.busticketapp.entity.User;
import springmockproject.busticketapp.error.UserError;
import springmockproject.busticketapp.login.Login;
import springmockproject.busticketapp.login.SessionUser;
import springmockproject.busticketapp.search.UserSearchInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import springmockproject.busticketapp.service.UserService;

@Slf4j
@Controller
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping(value = "/login")
    public String bruteForceLogin(Model model, HttpSession session) {
        model.addAttribute("login", new Login());
        return "login";
    }


    @GetMapping(value = "/home")
    public String redirectLogin(Model model, HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");

        if (isValidSessionUser(sessionUser)) {

            return redirectForSessionUser(sessionUser);

        } else {
            /*model.addAttribute("login", new Login());
            return "login";*/
            return "redirect:/customer/home";
        }

    }


    @PostMapping(value = "/login/submit")
    public String login(Login login, HttpSession session,
                        HttpServletRequest request,
                        String[] seatIds,
                        String cusViewTicketsAction,
                        String cusViewProfileAction,
                        String cusViewWalletAction,
                        Model model) {

        //TODO: Now every time the user does illegal login attempt this code segment will check
        // whether there's any living "session_user" object in their session:
        // If user tries to login using another account -> redirect to warning page which will display a message
        // telling that the user is trying to do double login attempt and offer to redirect them to their currently
        // logged-in homepage.
        // Else if user trying to login again using the same account id -> proceed to validate the login like usual,
        // whether if the user succeeds or fails or goes back to one of their current logged-in account's pages, it doesnt matter.

        SessionUser currentSessionUser = (SessionUser) session.getAttribute("session_user");

        if (currentSessionUser != null &&
                !currentSessionUser.getId().equals(login.getId())) {

            model.addAttribute("double_login_msg",
                    "It appears that you are currently logging in another account, " +
                            "in order to login another account you must log out from your current account" +
                            "you will be redirected to your currently logged in account's homepage!");

            return "user-warning";
        }

        //Else goes down here

        User checked = userService.getUserByLogin(login);

        if (checked != null) {

            boolean isFromBooking = seatIds != null && checked.getRole() == 0;
            boolean isFromViewingTickets = cusViewTicketsAction != null && checked.getRole() == 0;
            boolean isFromViewingProfile = cusViewProfileAction != null && checked.getRole() == 0;
            boolean isFromViewWallet = cusViewWalletAction != null && checked.getRole() == 0;

            SessionUser sessionUser = buildSessionUser(checked);
            session.setAttribute("session_user", sessionUser);


            if (isFromBooking) {
                log.info("IS FROM BOOKING");
                request.setAttribute("seatIds", seatIds);

                return "forward:/customer/book_seats";

            } else if (isFromViewingTickets) {
                return "redirect:/customer/view_my_tickets";

            } else if (isFromViewingProfile) {
                return "redirect:/customer/update_user_info";

            } else if (isFromViewWallet) {
                return "redirect:/customer/my_wallet";
            } else {
                return redirectForSessionUser(sessionUser);

            }
        } else {
            model.addAttribute("login_failed_msg", "Invalid username or password");
            model.addAttribute("seatIds", seatIds);
            model.addAttribute("cusViewTicketsAction", cusViewTicketsAction);
            model.addAttribute("cusViewProfileAction", cusViewProfileAction);
            model.addAttribute("login", new Login());
            return "login";
        }
    }

    private SessionUser buildSessionUser(User user) {
        return SessionUser.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    private boolean isValidSessionUser(SessionUser sessionUser) {

        return sessionUser != null &&
                sessionUser.getId() != null &&
                sessionUser.getFirstName() != null &&
                sessionUser.getLastName() != null &&
                sessionUser.getRole() >= 0 && sessionUser.getRole() < 4;


    }

    private String redirectForSessionUser(SessionUser sessionUser) {

        switch (sessionUser.getRole()) {
            case 0:
                return "redirect:/customer/home";
            case 1:
                return "redirect:/conductor/home";
            case 2:
                return "redirect:/admin/home";
            default:
                return "error";

        }

    }

    @GetMapping(value = "/register_form")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping(value = "/register/new")
    public String register(User user, Model model) {

        UserError errors = userService.validateRegistration(user);

        if (!errors.isValid()) {
            model.addAttribute("errors", errors);
            return "register";
        }

        userService.createUser(user);
        return "redirect:/home";
    }

    @GetMapping(value = "/admin/insert_user")
    public String showNewUserForm(Model model) {
        model.addAttribute("user", new User());
        return "insert-user";
    }

    @PostMapping(value = "/admin/insert_user/submit")
    public String insertNewUser(User user, Model model) {

        UserError errors = userService.validateRegistration(user);

        if (!errors.isValid()) {
            model.addAttribute("errors", errors);
            return "insert-user";
        }

        userService.createUser(user);

        return "admin";
    }

    @GetMapping("/customer/update_user_info")
    public String showPersonalInfoForm(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");

        if (sessionUser == null) {
            model.addAttribute("login", new Login());
            model.addAttribute("cusViewProfileAction", "yes");
            return "login";
        }

        User user = userService.searchById(sessionUser.getId());

        model.addAttribute("user", user);
        return "update-personal";
    }

    @PostMapping("/customer/update_user_info/submit")
    public String updatePersonalInfo(User user, HttpSession session, Model model) {
        //UserError errors = validator.validateUpdate(user);

        UserError errors = userService.validateUpdatedUser(user, false);

        if (!errors.isValid()) {
            model.addAttribute("errors", errors);
            model.addAttribute("user", user);
            return "update-personal";
        }

        userService.updateUser(user);

        SessionUser sessionUser = SessionUser.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(0)
                .build();

        session.setAttribute("session_user", sessionUser);

        return "redirect:/home";

    }

    @GetMapping("/admin/view_users")
    public String viewUsers(Model model) {
        List<User> allUsers = userService.searchAll();

        model.addAttribute("list_users", allUsers);
        model.addAttribute("searchInfo", new UserSearchInfo());
        return "user-list";
    }

    @GetMapping("/admin/search_users")
    public String navigateToSearchPage(Model model) {
        model.addAttribute("searchInfo", new UserSearchInfo());
        return "search-users";
    }

    @PostMapping("/admin/view_filtered_users")
    public String viewFilteredUsers(UserSearchInfo searchInfo, Model model) {
        List<User> users = userService.searchByInfo(searchInfo);

        model.addAttribute("list_users", users);
        return "user-list";
    }

    @GetMapping("/admin/update_user")
    public String prepareUpdatedUser(String userId, Model model) {
        //User selected = userRepo.getUserById(userId);

        User selected = userService.searchById(userId);

        model.addAttribute("updated", selected);
        return "update-user";
    }

    @PostMapping("/admin/update_user/submit")
    public String submitUserUpdate(User updated, String action, Model model) {

        if (action != null && action.equals("Reset Password")) {

            userService.resetUserPassword(updated);

        } else {

            UserError errors = userService.validateUpdatedUser(updated, true);

            if (!errors.isValid()) {
                model.addAttribute("errors", errors);
                model.addAttribute("updated", updated);
                return "update-user";
            }

            userService.updateUser(updated);
        }

        return "redirect:/admin/view_users";
    }

    @GetMapping("/admin/update_user_role")
    public String prepareRoleSwitch(String userId, Model model) {
        User selected = userService.searchById(userId);
        model.addAttribute("updated", selected);
        return "update-user-role";
    }

    @GetMapping("/admin/update_user_role/submit")
    public String submitRoleChange(User updated, String newRoleStr, HttpSession session, Model model) {

        int newRole = Integer.parseInt(newRoleStr);

        SessionUser sessionUser = (SessionUser) session.getAttribute("session_user");

        if (sessionUser.getId().equals(updated.getId())) {
            model.addAttribute("role_change_fail_msg", "You cannot change your own role");
            model.addAttribute("updated", updated);
            return "update-user-role";
        }

        if (updated.getRole() == newRole) {
            model.addAttribute("role_change_fail_msg", "Couldn't change to the same role");
            model.addAttribute("updated", updated);
            return "update-user-role";
        } else if (userService.checkIfUserHasActiveTickets(updated)) {
            model.addAttribute("role_change_fail_msg", "This customer is currently booking tickets! Role change failed");
            model.addAttribute("updated", updated);
            return "update-user-role";
        } else {
            userService.updateUserRole(newRole, updated);
            return "redirect:/admin/view_users";
        }


    }


}
