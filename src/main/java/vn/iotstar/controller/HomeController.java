package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import vn.iotstar.service.*;

@Controller
public class HomeController {

    private final UserService users;
    private final ProductService products;

    public HomeController(
            UserService users,
            ProductService products
    ) {
        this.users = users;
        this.products = products;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
            "userCount",
            users.count()
        );

        model.addAttribute(
            "productCount",
            products.count()
        );

        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}