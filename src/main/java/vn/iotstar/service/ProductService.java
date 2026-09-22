package vn.iotstar.service;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final ProductMapper productMapper;

    private final CloudinaryService cloudinaryService;


    public ProductService(
            ProductRepository productRepository,
            UserRepository userRepository,
            ProductMapper productMapper,
            CloudinaryService cloudinaryService
    ) {

        this.productRepository =
                productRepository;

        this.userRepository =
                userRepository;

        this.productMapper =
                productMapper;

        this.cloudinaryService =
                cloudinaryService;
    }


    /* =====================================================
       LIST / SEARCH / PAGINATION
       ===================================================== */

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(
            String keyword,
            int page,
            int size,
            CustomUserDetails principal
    ) {

        Pageable pageable =
                PageRequest.of(
                    page,
                    size,
                    Sort.by(
                        Sort.Direction.DESC,
                        "id"
                    )
                );


        Page<Product> result;


        /*
         * ADMIN:
         *
         * Xem tất cả Product.
         */
        if (isAdmin(principal)) {

            if (keyword == null
                    || keyword.isBlank()) {

                result =
                    productRepository.findAll(
                        pageable
                    );

            } else {

                result =
                    productRepository
                        .findByNameContainingIgnoreCase(
                            keyword.trim(),
                            pageable
                        );
            }
        }


        /*
         * USER:
         *
         * Chỉ xem Product của chính mình.
         */
        else {

            if (keyword == null
                    || keyword.isBlank()) {

                result =
                    productRepository
                        .findByUserId(
                            principal.getId(),
                            pageable
                        );

            } else {

                result =
                    productRepository
                        .findByUserIdAndNameContainingIgnoreCase(
                            principal.getId(),
                            keyword.trim(),
                            pageable
                        );
            }
        }


        return result.map(
            productMapper::toDTO
        );
    }


    /* =====================================================
       FIND BY ID
       ===================================================== */

    @Transactional(readOnly = true)
    public ProductDTO findById(
            Long id,
            CustomUserDetails principal
    ) {

        Product product =
                findProductWithOwner(id);


        checkPermission(
            product,
            principal
        );


        return productMapper.toDTO(
            product
        );
    }


    /* =====================================================
       CREATE
       ===================================================== */

    @Transactional
    public void create(
            ProductDTO dto,
            MultipartFile file,
            CustomUserDetails principal
    ) throws IOException {

        Product product =
                new Product();


        /*
         * ================================================
         * OWNER
         * ================================================
         *
         * ADMIN:
         * Có thể chọn User owner từ form.
         *
         * USER:
         * Product tự động thuộc tài khoản đang login.
         */

        User owner;


        if (isAdmin(principal)) {

            if (dto.getUserId() == null) {

                throw new IllegalArgumentException(
                    "Vui lòng chọn chủ sở hữu."
                );
            }


            owner =
                userRepository
                    .findById(dto.getUserId())
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Không tìm thấy User ID: "
                                    + dto.getUserId()
                            )
                    );

        } else {

            owner =
                userRepository
                    .findById(principal.getId())
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Không tìm thấy tài khoản đang đăng nhập."
                            )
                    );
        }


        product.setName(
            dto.getName()
        );

        product.setDescription(
            dto.getDescription()
        );

        product.setPrice(
            dto.getPrice()
        );

        product.setUser(
            owner
        );


        /*
         * IMAGE
         */

        if (file != null
                && !file.isEmpty()) {

            String imageUrl =
                cloudinaryService.upload(
                    file
                );


            if (imageUrl != null
                    && !imageUrl.isBlank()) {

                product.setImage(
                    imageUrl
                );
            }
        }


        productRepository.save(
            product
        );
    }


    /* =====================================================
       UPDATE
       ===================================================== */

    @Transactional
    public void update(
            ProductDTO dto,
            MultipartFile file,
            CustomUserDetails principal
    ) throws IOException {

        if (dto.getId() == null) {

            throw new IllegalArgumentException(
                "Product ID không được để trống."
            );
        }


        Product product =
                findProductWithOwner(
                    dto.getId()
                );


        /*
         * USER phải sở hữu Product.
         *
         * ADMIN luôn được phép.
         */
        checkPermission(
            product,
            principal
        );


        product.setName(
            dto.getName()
        );

        product.setDescription(
            dto.getDescription()
        );

        product.setPrice(
            dto.getPrice()
        );


        /*
         * ADMIN được phép thay owner.
         *
         * USER KHÔNG được đổi owner.
         */
        if (isAdmin(principal)
                && dto.getUserId() != null) {

            User newOwner =
                userRepository
                    .findById(dto.getUserId())
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Không tìm thấy User ID: "
                                    + dto.getUserId()
                            )
                    );


            product.setUser(
                newOwner
            );
        }


        /*
         * Upload ảnh mới nếu có.
         *
         * Không upload => giữ ảnh cũ.
         */
        if (file != null
                && !file.isEmpty()) {

            String imageUrl =
                cloudinaryService.upload(
                    file
                );


            if (imageUrl != null
                    && !imageUrl.isBlank()) {

                product.setImage(
                    imageUrl
                );
            }
        }


        productRepository.save(
            product
        );
    }


    /* =====================================================
       DELETE
       ===================================================== */

    @Transactional
    public void delete(
            Long id,
            CustomUserDetails principal
    ) {

        Product product =
                findProductWithOwner(id);


        checkPermission(
            product,
            principal
        );


        productRepository.delete(
            product
        );
    }


    /* =====================================================
       COUNT ALL
       ADMIN DASHBOARD
       ===================================================== */

    @Transactional(readOnly = true)
    public long count() {

        return productRepository.count();
    }


    /* =====================================================
       COUNT PRODUCT BY USER
       ===================================================== */

    @Transactional(readOnly = true)
    public long countByUserId(
            Long userId
    ) {

        return productRepository
                .countByUserId(userId);
    }


    /* =====================================================
       PRIVATE - LOAD PRODUCT + USER
       ===================================================== */

    private Product findProductWithOwner(
            Long id
    ) {

        return productRepository
                .findWithUserById(id)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Không tìm thấy sản phẩm ID: "
                                + id
                        )
                );
    }


    /* =====================================================
       PRIVATE - CHECK ADMIN
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


    /* =====================================================
       PRIVATE - CHECK OWNERSHIP
       ===================================================== */

    private void checkPermission(
            Product product,
            CustomUserDetails principal
    ) {

        /*
         * ADMIN:
         * được phép thao tác mọi Product.
         */
        if (isAdmin(principal)) {
            return;
        }


        /*
         * USER:
         * owner phải chính là user đang login.
         */

        Long ownerId =
                product
                    .getUser()
                    .getId();


        if (!ownerId.equals(
                principal.getId()
        )) {

            throw new AccessDeniedException(
                "Bạn không có quyền thao tác sản phẩm của người dùng khác."
            );
        }
    }
}