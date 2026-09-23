package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final ProductRepository products;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository users,
            RoleRepository roles,
            ProductRepository products,
            UserMapper mapper,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.roles = roles;
        this.products = products;
        this.mapper = mapper;
        this.passwordEncoder =
            passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable =
            PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by("id").descending()
            );

        Page<User> result;

        if (keyword == null
                || keyword.isBlank()) {

            result = users.findAll(pageable);

        } else {

            String search =
                keyword.trim();

            result =
                users
                    .findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                        search,
                        search,
                        search,
                        pageable
                    );
        }

        return result.map(user -> {

            UserDTO dto =
                mapper.toDTO(user);

            dto.setProductCount(
                products.countByUserId(
                    user.getId()
                )
            );

            return dto;
        });
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {

        User user =
            users.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Không tìm thấy user."
                    )
                );

        UserDTO dto =
            mapper.toDTO(user);

        dto.setProductCount(
            products.countByUserId(id)
        );

        return dto;
    }

    @Transactional
    public void create(UserDTO dto) {

        String username =
            required(
                dto.getUsername(),
                "Username"
            );

        String email =
            required(
                dto.getEmail(),
                "Email"
            ).toLowerCase();

        String fullName =
            required(
                dto.getFullName(),
                "Họ tên"
            );

        if (users.existsByUsernameIgnoreCase(
                username
        )) {
            throw new IllegalArgumentException(
                "Username đã tồn tại."
            );
        }

        if (users.existsByEmailIgnoreCase(
                email
        )) {
            throw new IllegalArgumentException(
                "Email đã tồn tại."
            );
        }

        Role role =
            findRole(dto.getRoleId());

        User user =
            new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);

        /*
         * Theo đề mẫu:
         * Admin tạo User -> password mặc định 123456.
         */
        user.setPassword(
            passwordEncoder.encode(
                "123456"
            )
        );

        user.setEnabled(
            dto.isEnabled()
        );

        user.setRole(role);

        users.save(user);
    }

    @Transactional
    public void update(
            Long id,
            UserDTO dto
    ) {

        User user =
            users.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Không tìm thấy user."
                    )
                );

        String username =
            required(
                dto.getUsername(),
                "Username"
            );

        String email =
            required(
                dto.getEmail(),
                "Email"
            ).toLowerCase();

        String fullName =
            required(
                dto.getFullName(),
                "Họ tên"
            );

        users
            .findByUsernameIgnoreCase(username)
            .filter(other ->
                !other.getId().equals(id)
            )
            .ifPresent(other -> {
                throw new IllegalArgumentException(
                    "Username đã tồn tại."
                );
            });

        users
            .findByEmailIgnoreCase(email)
            .filter(other ->
                !other.getId().equals(id)
            )
            .ifPresent(other -> {
                throw new IllegalArgumentException(
                    "Email đã tồn tại."
                );
            });

        Role role =
            findRole(dto.getRoleId());

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setEnabled(
            dto.isEnabled()
        );
        user.setRole(role);

        users.save(user);
    }

    @Transactional
    public void delete(Long id) {

        User user =
            users.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Không tìm thấy user."
                    )
                );

        users.delete(user);
    }

    @Transactional(readOnly = true)
    public long count() {
        return users.count();
    }

    private Role findRole(
            Long roleId
    ) {

        if (roleId == null) {
            throw new IllegalArgumentException(
                "Vui lòng chọn vai trò."
            );
        }

        return roles
            .findById(roleId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Không tìm thấy vai trò."
                )
            );
    }

    private String required(
            String value,
            String field
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                field
                + " không được để trống."
            );
        }

        return value.trim();
    }
}