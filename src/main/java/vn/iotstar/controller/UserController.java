package vn.iotstar.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        Page<UserDTO> users =
            service.findAll(
                keyword,
                page,
                5
            );

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);

        return "users/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
            "user",
            service.findById(id)
        );

        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute UserDTO dto
    ) {

        service.update(id, dto);

        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id
    ) {

        service.delete(id);

        return "redirect:/users";
    }
}