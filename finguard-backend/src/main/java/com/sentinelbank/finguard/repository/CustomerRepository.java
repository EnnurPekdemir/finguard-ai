package com.sentinelbank.finguard.repository;

import com.sentinelbank.finguard.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Database access layer for Customer entity.
 *
 * <p>Standard CRUD operations (save, findById, findAll, deleteById, etc.)
 * are automatically provided by {@link JpaRepository}.</p>
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a customer by National Identity Number.
     *
     * @param identityNumber 11-digit national identity number
     * @return Optional containing the found customer, or empty
     */
    Optional<Customer> findByIdentityNumber(String identityNumber);

    /**
     * Checks if a customer exists with the given National Identity Number.
     *
     * @param identityNumber 11-digit national identity number
     * @return true if exists, false otherwise
     */
    boolean existsByIdentityNumber(String identityNumber);

    /**
     * Finds a customer by email address.
     *
     * @param email customer email address
     * @return Optional containing the found customer, or empty
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Checks if a customer exists with the given email address.
     *
     * @param email customer email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
}
