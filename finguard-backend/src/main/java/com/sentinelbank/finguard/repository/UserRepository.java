package com.sentinelbank.finguard.repository;

import com.sentinelbank.finguard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User database access.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Search by username.
     *
     * @param username username to search
     * @return optional containing the found user, or empty
     */
    Optional<User> findByUsername(String username);

    /**
     * Search by email address.
     *
     * @param email email to search
     * @return optional containing the found user, or empty
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given email.
     */
    boolean existsByEmail(String email);
}
