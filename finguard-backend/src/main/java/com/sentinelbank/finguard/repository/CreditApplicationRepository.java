package com.sentinelbank.finguard.repository;

import com.sentinelbank.finguard.model.ApplicationStatus;
import com.sentinelbank.finguard.model.CreditApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database access layer for CreditApplication entity.
 *
 * <p>Standard CRUD operations (save, findById, findAll, deleteById, etc.)
 * are automatically provided by {@link JpaRepository}.</p>
 */
@Repository
public interface CreditApplicationRepository extends JpaRepository<CreditApplication, Long> {

    /**
     * Gets all credit applications belonging to a specific customer.
     *
     * @param customerId customer ID
     * @return list of applications (can be empty)
     */
    List<CreditApplication> findByCustomerId(Long customerId);

    /**
     * Gets all applications with a specific status.
     * For example, all {@code PENDING} or {@code MANUAL_REVIEW} applications.
     *
     * @param status application status
     * @return list of applications
     */
    List<CreditApplication> findByStatus(ApplicationStatus status);

    /**
     * Gets applications belonging to a customer with a specific status.
     *
     * @param customerId customer ID
     * @param status     application status
     * @return filtered list of applications
     */
    List<CreditApplication> findByCustomerIdAndStatus(Long customerId, ApplicationStatus status);
}
