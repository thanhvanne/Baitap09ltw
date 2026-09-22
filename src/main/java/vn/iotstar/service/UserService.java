package vn.iotstar.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.*;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.*;

@Service
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final ProductRepository products;
    private final UserMapper mapper;

    public UserService(
            UserRepository users,
            RoleRepository roles,
            ProductRepository products,
            UserMapper mapper
    ) {
        this.users = users;
        this.roles = roles;
        this.products = products;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
            );

        Page<User> result;

        if (keyword == null || keyword.isBlank()) {

            result = users.findAll(pageable);

        } else {

            result =
                users
                .findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                    keyword,
                    keyword,
                    keyword,
                    pageable
                );
        }

        return result.map(user -> {

            UserDTO dto = mapper.toDTO(user);

            dto.setProductCount(
                products.countByUserId(user.getId())
            );

            return dto;
        });
    }

    public UserDTO findById(Long id) {

        return mapper.toDTO(
            users.findById(id)
                .orElseThrow()
        );
    }

    @Transactional
    public void update(Long id, UserDTO dto) {

        User user = users.findById(id)
            .orElseThrow();

        Role role = roles
            .findById(dto.getRoleId())
            .orElseThrow();

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setEnabled(dto.isEnabled());
        user.setRole(role);

        users.save(user);
    }

    public void delete(Long id) {
        users.deleteById(id);
    }

    public long count() {
        return users.count();
    }
}