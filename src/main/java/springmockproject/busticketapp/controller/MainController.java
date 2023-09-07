package springmockproject.busticketapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class MainController {
    @RequestMapping("/")
    public String loginPage() {
        return "redirect:home";
    }

    @GetMapping("/admin/home")
    public String adminHome() {
        return "admin";
    }

    @GetMapping("/customer/home")
    public String customerHome() {
        return "redirect:/customer/view_buses";
    }

    @GetMapping("/conductor/home")
    public String conductorHome() {
        return "conductor";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:home";
    }


}
