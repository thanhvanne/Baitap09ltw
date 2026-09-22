package vn.iotstar.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {

        User user = userRepository
            .findByLogin(login)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Không tìm thấy username/email: " + login
                )
            );

        return new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            user.getFullName(),
            user.getImages(),
            user.getRole().getName(),
            user.isEnabled()
        );
    }
}