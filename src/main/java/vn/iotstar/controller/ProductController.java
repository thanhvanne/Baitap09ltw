package vn.iotstar.controller;

import java.io.IOException;

import org.springframework.data.domain.Page;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

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


    /* =====================================================
       LIST
       ===================================================== */

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


    /* =====================================================
       CREATE FORM
       ===================================================== */

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


        /*
         * Chỉ ADMIN cần danh sách User
         * để chọn owner.
         */

        if (isAdmin(principal)) {

            model.addAttribute(
                "users",
                userRepository.findAll()
            );
        }


        return "products/form";
    }


    /* =====================================================
       EDIT FORM
       ===================================================== */

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CustomUserDetails principal,

            Model model
    ) {

        ProductDTO product =
                productService.findById(
                    id,
                    principal
                );


        model.addAttribute(
            "product",
            product
        );


        if (isAdmin(principal)) {

            model.addAttribute(
                "users",
                userRepository.findAll()
            );
        }


        return "products/form";
    }


    /* =====================================================
       SAVE CREATE / UPDATE
       ===================================================== */

    @PostMapping("/save")
    public String save(
            @ModelAttribute("product")
            ProductDTO dto,

            @RequestParam(
                name = "file",
                required = false
            )
            MultipartFile file,

            @AuthenticationPrincipal
            CustomUserDetails principal
    ) throws IOException {


        /*
         * ID null => CREATE
         */
        if (dto.getId() == null) {

            productService.create(
                dto,
                file,
                principal
            );

        }


        /*
         * Có ID => UPDATE
         */
        else {

            productService.update(
                dto,
                file,
                principal
            );
        }


        return "redirect:/products";
    }


    /* =====================================================
       DELETE
       ===================================================== */

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,

            @AuthenticationPrincipal
            CustomUserDetails principal
    ) {

        productService.delete(
            id,
            principal
        );


        return "redirect:/products";
    }


    /* =====================================================
       HELPER
       ===================================================== */

    private boolean isAdmin(
            CustomUserDetails principal
    ) {

        return principal
                .getAuthorities()
                .stream()
                .anyMatch(
                    authority ->
                        "ROLE_ADMIN".equals(
                            authority.getAuthority()
                        )
                );
    }
}