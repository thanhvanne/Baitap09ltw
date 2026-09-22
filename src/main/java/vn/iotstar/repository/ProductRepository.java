package vn.iotstar.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long> {


    @Override
    @EntityGraph(attributePaths = "user")
    Page<Product> findAll(
            Pageable pageable
    );


    @EntityGraph(attributePaths = "user")
    Page<Product> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );


    /*
     * Danh sách Product của một User.
     */
    @EntityGraph(attributePaths = "user")
    Page<Product> findByUserId(
            Long userId,
            Pageable pageable
    );


    /*
     * Search Product nhưng chỉ của User đó.
     */
    @EntityGraph(attributePaths = "user")
    Page<Product> findByUserIdAndNameContainingIgnoreCase(
            Long userId,
            String name,
            Pageable pageable
    );


    /*
     * Load Product + owner.
     */
    @EntityGraph(attributePaths = "user")
    Optional<Product> findWithUserById(
            Long id
    );


    long countByUserId(
            Long userId
    );
}