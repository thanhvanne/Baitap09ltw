package vn.iotstar.controller;

import java.io.IOException;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iotstar.dto.ProductDTO;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(
            ProductService productService,
            UserRepository userRepository
    ) {
        this.productService =
            productService;

        this.userRepository =
            userRepository;
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

            @AuthenticationPrincipal
            CustomUserDetails principal,

            Model model
    ) {

        Page<ProductDTO> products =
            productService.findAll(
                keyword,
                page,
                5,
                principal
            );

        model.addAttribute(
            "products",
            products
        );

        model.addAttribute(
            "keyword",
            keyword
        );

        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(
            @AuthenticationPrincipal
            CustomUserDetails principal,

            Model model
    ) {

        model.addAttribute(
            "product",
            new ProductDTO()
        );

        prepareUsers(
            principal,
            model
        );

        return "products/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CustomUserDetails principal,

            Model model
    ) {

        model.addAttribute(
            "product",
            productService.findById(
                id,
                principal
            )
        );

        prepareUsers(
            principal,
            model
        );

        return "products/form";
    }

    @PostMapping("/save")
    public String save(
            @Valid
            @ModelAttribute("product")
            ProductDTO dto,

            BindingResult result,

            @RequestParam(
                name = "file",
                required = false
            )
            MultipartFile file,

            @AuthenticationPrincipal
            CustomUserDetails principal,

            Model model,
            RedirectAttributes redirect
    ) throws IOException {

        if (result.hasErrors()) {

            prepareUsers(
                principal,
                model
            );

            return "products/form";
        }

        try {

            if (dto.getId() == null) {

                productService.create(
                    dto,
                    file,
                    principal
                );

                redirect.addFlashAttribute(
                    "success",
                    "Thêm sản phẩm thành công."
                );

            } else {

                productService.update(
                    dto,
                    file,
                    principal
                );

                redirect.addFlashAttribute(
                    "success",
                    "Cập nhật sản phẩm thành công."
                );
            }

            return "redirect:/products";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                "error",
                e.getMessage()
            );

            prepareUsers(
                principal,
                model
            );

            return "products/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CustomUserDetails principal,

            RedirectAttributes redirect
    ) {

        productService.delete(
            id,
            principal
        );

        redirect.addFlashAttribute(
            "success",
            "Xóa sản phẩm thành công."
        );

        return "redirect:/products";
    }

    private void prepareUsers(
            CustomUserDetails principal,
            Model model
    ) {

        if (isAdmin(principal)) {

            model.addAttribute(
                "users",
                userRepository.findAll()
            );
        }
    }

    private boolean isAdmin(
            CustomUserDetails principal
    ) {

        return principal
            .getAuthorities()
            .stream()
            .anyMatch(authority ->
                "ROLE_ADMIN".equals(
                    authority.getAuthority()
                )
            );
    }
}