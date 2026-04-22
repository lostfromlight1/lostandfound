package com.lostandfound.app.repository;

import com.lostandfound.app.model.Role;
import com.lostandfound.app.model.User;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Essential for Spring Security's UserDetailsService to load a user during login.
     */
    Optional<User> findByEmail(String email);

    /**
     * Useful for checking if an email is already taken during user registration.
     */
    boolean existsByEmail(String email);

    @NonNull
    @Query(
            """
            SELECT u FROM User u
            WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<User> searchUsers(@Param("query") String query, @NonNull Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE u.role = :role AND u.active = true
    """)
    List<User> findByRole(@Param("role") Role role);
}