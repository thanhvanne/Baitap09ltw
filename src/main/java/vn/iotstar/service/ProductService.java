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

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(
            String keyword,
            int page,
            int size,
            CustomUserDetails principal
    ) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        Pageable pageable =
            PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                    Sort.Direction.DESC,
                    "id"
                )
            );

        String search =
            keyword == null
                ? ""
                : keyword.trim();

        Page<Product> result;

        if (isAdmin(principal)) {

            result =
                search.isBlank()
                    ? productRepository.findAll(pageable)
                    : productRepository
                        .findByNameContainingIgnoreCase(
                            search,
                            pageable
                        );

        } else {

            result =
                search.isBlank()
                    ? productRepository.findByUserId(
                        principal.getId(),
                        pageable
                    )
                    : productRepository
                        .findByUserIdAndNameContainingIgnoreCase(
                            principal.getId(),
                            search,
                            pageable
                        );
        }

        return result.map(
            productMapper::toDTO
        );
    }

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

    @Transactional
    public void create(
            ProductDTO dto,
            MultipartFile file,
            CustomUserDetails principal
    ) throws IOException {

        User owner =
            resolveOwnerForCreate(
                dto,
                principal
            );

        Product product =
            new Product();

        applyEditableFields(
            product,
            dto
        );

        product.setUser(owner);

        if (file != null
                && !file.isEmpty()) {

            CloudinaryService.UploadResult upload =
                cloudinaryService.upload(file);

            if (upload != null) {
                product.setImage(
                    upload.url()
                );

                product.setImagePublicId(
                    upload.publicId()
                );
            }
        }

        productRepository.save(product);
    }

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

        checkPermission(
            product,
            principal
        );

        applyEditableFields(
            product,
            dto
        );

        if (isAdmin(principal)) {

            if (dto.getUserId() == null) {
                throw new IllegalArgumentException(
                    "Vui lòng chọn chủ sở hữu."
                );
            }

            User owner =
                userRepository
                    .findById(dto.getUserId())
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "Không tìm thấy User ID: "
                            + dto.getUserId()
                        )
                    );

            product.setUser(owner);
        }

        if (file != null
                && !file.isEmpty()) {

            CloudinaryService.UploadResult upload =
                cloudinaryService.upload(file);

            if (upload != null) {

                String oldPublicId =
                    product.getImagePublicId();

                product.setImage(
                    upload.url()
                );

                product.setImagePublicId(
                    upload.publicId()
                );

                /*
                 * Product mới phải được chuẩn bị xong
                 * trước rồi mới dọn ảnh cũ.
                 */
                if (oldPublicId != null
                        && !oldPublicId.isBlank()) {

                    cloudinaryService.delete(
                        oldPublicId
                    );
                }
            }
        }

        productRepository.save(product);
    }

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

        String publicId =
            product.getImagePublicId();

        productRepository.delete(product);

        if (publicId != null
                && !publicId.isBlank()) {

            try {
                cloudinaryService.delete(
                    publicId
                );
            } catch (IOException e) {
                throw new IllegalStateException(
                    "Không thể xóa ảnh trên Cloudinary.",
                    e
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public long count() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByUserId(
            Long userId
    ) {
        return productRepository
            .countByUserId(userId);
    }

    private User resolveOwnerForCreate(
            ProductDTO dto,
            CustomUserDetails principal
    ) {

        Long userId =
            isAdmin(principal)
                ? dto.getUserId()
                : principal.getId();

        if (userId == null) {
            throw new IllegalArgumentException(
                "Vui lòng chọn chủ sở hữu."
            );
        }

        return userRepository
            .findById(userId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Không tìm thấy User ID: "
                    + userId
                )
            );
    }

    private void applyEditableFields(
            Product product,
            ProductDTO dto
    ) {

        product.setName(
            dto.getName().trim()
        );

        product.setDescription(
            dto.getDescription() == null
                ? null
                : dto.getDescription().trim()
        );

        product.setPrice(
            dto.getPrice()
        );
    }

    private Product findProductWithOwner(
            Long id
    ) {

        return productRepository
            .findWithUserById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Không tìm thấy sản phẩm ID: "
                    + id
                )
            );
    }

    private boolean isAdmin(
            CustomUserDetails principal
    ) {

        return principal != null
            && principal
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                    "ROLE_ADMIN".equals(
                        authority.getAuthority()
                    )
                );
    }

    private void checkPermission(
            Product product,
            CustomUserDetails principal
    ) {

        if (principal == null) {
            throw new AccessDeniedException(
                "Bạn chưa đăng nhập."
            );
        }

        if (isAdmin(principal)) {
            return;
        }

        if (!product
                .getUser()
                .getId()
                .equals(principal.getId())) {

            throw new AccessDeniedException(
                "Bạn không có quyền thao tác sản phẩm này."
            );
        }
    }
}