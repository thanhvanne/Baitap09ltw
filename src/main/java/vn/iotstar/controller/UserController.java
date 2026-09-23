package vn.iotstar.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService service;
    private final RoleRepository roleRepository;

    public UserController(
            UserService service,
            RoleRepository roleRepository
    ) {
        this.service = service;
        this.roleRepository =
            roleRepository;
    }

    @GetMapping
    public String list(
            @RequestParam(
                defaultValue = ""
            )
            String keyword,

            @RequestParam(
                defaultValue = "0"
            )
            int page,

            Model model
    ) {

        Page<UserDTO> users =
            service.findAll(
                keyword,
                page,
                5
            );

        model.addAttribute(
            "users",
            users
        );

        model.addAttribute(
            "keyword",
            keyword
        );

        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(
            Model model
    ) {

        UserDTO user =
            new UserDTO();

        user.setEnabled(true);

        roleRepository
            .findByNameIgnoreCase("USER")
            .ifPresent(role ->
                user.setRoleId(
                    role.getId()
                )
            );

        prepareForm(
            model,
            user,
            true
        );

        return "users/form";
    }

    @PostMapping("/new")
    public String create(
            @ModelAttribute("user")
            UserDTO dto,

            Model model,
            RedirectAttributes redirect
    ) {

        try {

            service.create(dto);

            redirect.addFlashAttribute(
                "success",
                "Tạo user thành công. "
                + "Mật khẩu mặc định: 123456"
            );

            return "redirect:/users";

        } catch (IllegalArgumentException e) {

            prepareForm(
                model,
                dto,
                true
            );

            model.addAttribute(
                "error",
                e.getMessage()
            );

            return "users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model
    ) {

        prepareForm(
            model,
            service.findById(id),
            false
        );

        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id,

            @ModelAttribute("user")
            UserDTO dto,

            Model model,
            RedirectAttributes redirect
    ) {

        try {

            service.update(
                id,
                dto
            );

            redirect.addFlashAttribute(
                "success",
                "Cập nhật user thành công."
            );

            return "redirect:/users";

        } catch (IllegalArgumentException e) {

            dto.setId(id);

            prepareForm(
                model,
                dto,
                false
            );

            model.addAttribute(
                "error",
                e.getMessage()
            );

            return "users/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirect
    ) {

        try {

            service.delete(id);

            redirect.addFlashAttribute(
                "success",
                "Xóa user thành công."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                "error",
                e.getMessage()
            );
        }

        return "redirect:/users";
    }

    private void prepareForm(
            Model model,
            UserDTO user,
            boolean create
    ) {

        model.addAttribute(
            "user",
            user
        );

        model.addAttribute(
            "roles",
            roleRepository.findAll()
        );

        model.addAttribute(
            "createMode",
            create
        );
    }
}