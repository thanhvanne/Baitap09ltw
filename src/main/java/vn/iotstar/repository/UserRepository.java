package vn.iotstar.repository;

import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import vn.iotstar.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        SELECT u
        FROM User u
        JOIN FETCH u.role
        WHERE LOWER(u.username) = LOWER(:login)
           OR LOWER(u.email) = LOWER(:login)
    """)
    Optional<User> findByLogin(@Param("login") String login);

    Page<User>
    findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
        String email,
        String fullName,
        String username,
        Pageable pageable
    );
}