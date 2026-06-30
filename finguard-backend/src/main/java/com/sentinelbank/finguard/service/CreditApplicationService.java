package com.sentinelbank.finguard.service;

import com.sentinelbank.finguard.dto.CreditApplicationRequestDTO;
import com.sentinelbank.finguard.dto.MLPredictionResponseDTO;
import com.sentinelbank.finguard.model.ApplicationStatus;
import com.sentinelbank.finguard.model.CreditApplication;
import com.sentinelbank.finguard.model.Customer;
import com.sentinelbank.finguard.repository.CreditApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Credit application business logic service layer.
 *
 * <p>Handles application creation, retrieval, status updates, and ML service integration.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreditApplicationService {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CustomerService customerService;
    private final MLClientService mlClientService;

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────

    /**
     * Creates a new credit application and queries the ML service.
     *
     * @param request DTO containing application data and risk parameters
     * @return Saved credit application updated with ML decision
     */
    public CreditApplication createApplication(CreditApplicationRequestDTO request) {
        log.info("New credit application request received. Customer ID: {}, Requested Amount: {} TL", 
                request.getCustomerId(), request.getRequestedAmount());

        if (request.getCustomerId() == null) {
            log.error("Credit application error: Customer ID is empty.");
            throw new IllegalArgumentException("Customer ID cannot be empty.");
        }
        if (request.getRequestedAmount() == null || request.getRequestedAmount() <= 0) {
            log.error("Credit application error: Invalid requested amount: {}", request.getRequestedAmount());
            throw new IllegalArgumentException("Requested loan amount must be greater than zero.");
        }

        // Retrieve customer (throws exception if not found)
        Customer customer = customerService.getCustomerById(request.getCustomerId());

        // Create the application with PENDING status initially
        CreditApplication application = CreditApplication.builder()
            .customer(customer)
            .requestedAmount(request.getRequestedAmount())
            .status(ApplicationStatus.PENDING)
            .build();

        // Perform initial save (to obtain auto-generated ID)
        application = creditApplicationRepository.save(application);
        log.debug("Application temporarily saved with status PENDING. Application ID: {}", application.getId());

        // Query the FastAPI ML service for risk score
        try {
            log.info("Querying risk analysis from FastAPI ML service. Application ID: {}", application.getId());
            MLPredictionResponseDTO prediction = mlClientService.predictCreditRisk(request, customer);

            // Update application with the prediction details
            application.setEntropyScore(prediction.getEntropyScore());

            String decisionFlow = prediction.getDecisionFlow();
            log.info("Response received from ML service. Decision Flow: {}, Uncertainty Score (Entropy): {}", 
                    decisionFlow, prediction.getEntropyScore());

            if ("SISTEM_ONAY".equalsIgnoreCase(decisionFlow)) {
                application.setStatus(ApplicationStatus.APPROVED);
                log.info("Application automatically approved. Application ID: {}", application.getId());
            } else if ("SISTEM_RED".equalsIgnoreCase(decisionFlow)) {
                application.setStatus(ApplicationStatus.REJECTED);
                log.info("Application automatically rejected. Application ID: {}", application.getId());
            } else if ("MANUEL_INCELEME".equalsIgnoreCase(decisionFlow)) {
                application.setStatus(ApplicationStatus.MANUAL_REVIEW);
                log.warn("WARNING: High decision uncertainty (Entropy > 0.8)! Application sent for manual review. Application ID: {}, Entropy: {}", 
                        application.getId(), prediction.getEntropyScore());
            } else {
                // Fallback: decide based on prediction flag
                if (prediction.getPrediction() != null && prediction.getPrediction() == 1) {
                    application.setStatus(ApplicationStatus.REJECTED);
                    log.info("Application rejected as fallback. Application ID: {}", application.getId());
                } else {
                    application.setStatus(ApplicationStatus.APPROVED);
                    log.info("Application approved as fallback. Application ID: {}", application.getId());
                }
            }

            // Save the updated application
            application = creditApplicationRepository.save(application);

        } catch (Exception e) {
            log.error("Critical error occurred during communication with ML service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect to risk analysis engine while evaluating credit application: " + e.getMessage(), e);
        }

        return application;
    }

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /**
     * Retrieves a credit application by ID.
     *
     * @param id Application ID
     * @return Found credit application
     * @throws IllegalArgumentException if the application is not found
     */
    @Transactional(readOnly = true)
    public CreditApplication getApplicationById(Long id) {
        return creditApplicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Credit application not found. ID: " + id
            ));
    }

    /**
     * Lists all credit applications.
     *
     * @return List of applications
     */
    @Transactional(readOnly = true)
    public List<CreditApplication> getAllApplications() {
        return creditApplicationRepository.findAll();
    }

    /**
     * Gets all credit applications belonging to a specific customer.
     *
     * @param customerId Customer ID
     * @return List of applications belonging to the customer
     */
    @Transactional(readOnly = true)
    public List<CreditApplication> getApplicationsByCustomerId(Long customerId) {
        // Verify customer exists
        customerService.getCustomerById(customerId);
        return creditApplicationRepository.findByCustomerId(customerId);
    }

    /**
     * Lists all applications with a specific status.
     * Useful for fetching PENDING or MANUAL_REVIEW applications.
     *
     * @param status Status to filter
     * @return Filtered list of applications
     */
    @Transactional(readOnly = true)
    public List<CreditApplication> getApplicationsByStatus(ApplicationStatus status) {
        return creditApplicationRepository.findByStatus(status);
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────

    /**
     * Updates the status of an application.
     *
     * @param id     Application ID
     * @param status New status
     * @return Updated application
     * @throws IllegalArgumentException if application is not found
     */
    public CreditApplication updateApplicationStatus(Long id, ApplicationStatus status) {
        CreditApplication application = getApplicationById(id);
        application.setStatus(status);
        return creditApplicationRepository.save(application);
    }

    /**
     * Updates both the entropy score and status of an application.
     *
     * @param id           Application ID
     * @param entropyScore Shannon Entropy score (0.0 - 1.0)
     * @param status       New status determined by ML result
     * @return Updated application
     * @throws IllegalArgumentException if application is not found
     */
    public CreditApplication updateApplicationWithMLResult(Long id, Double entropyScore, ApplicationStatus status) {
        CreditApplication application = getApplicationById(id);
        application.setEntropyScore(entropyScore);
        application.setStatus(status);
        return creditApplicationRepository.save(application);
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────

    /**
     * Deletes a credit application.
     *
     * @param id Application ID to be deleted
     * @throws IllegalArgumentException if application is not found
     */
    public void deleteApplication(Long id) {
        CreditApplication application = getApplicationById(id);
        creditApplicationRepository.delete(application);
    }
}
