package vn.iotstar.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import vn.iotstar.dto.*;
import vn.iotstar.service.AuthService;

@Controller
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute(
            "registerDTO",
            new RegisterDTO()
        );

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegisterDTO dto,
            BindingResult result,
            Model model
    ) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {

            auth.register(dto);

            return "redirect:/verify-otp?email="
                + dto.getEmail();

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                "error",
                e.getMessage()
            );

            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            Model model
    ) {

        model.addAttribute("email", email);

        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            Model model
    ) {

        try {

            auth.verifyRegister(email, otp);

            return "redirect:/login?verified=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());

            return "auth/verify-otp";
        }
    }

    @PostMapping("/register/resend-otp")
    public String resend(
            @RequestParam String email
    ) {

        auth.resendRegisterOtp(email);

        return "redirect:/verify-otp?email="
            + email;
    }

    @GetMapping("/forgot-password")
    public String forgot() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(
            @RequestParam String email,
            Model model
    ) {

        try {

            auth.forgotPassword(email);

            return "redirect:/reset-password?email="
                + email;

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                "error",
                e.getMessage()
            );

            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String reset(
            @RequestParam String email,
            Model model
    ) {

        ResetPasswordDTO dto =
            new ResetPasswordDTO();

        dto.setEmail(email);

        model.addAttribute(
            "resetDTO",
            dto
        );

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(
            @ModelAttribute ResetPasswordDTO dto,
            Model model
    ) {

        try {

            auth.resetPassword(dto);

            return "redirect:/login?reset=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                "error",
                e.getMessage()
            );

            return "auth/reset-password";
        }
    }
}