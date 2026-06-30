package com.sentinelbank.finguard.controller;

import com.sentinelbank.finguard.dto.CreditApplicationRequestDTO;
import com.sentinelbank.finguard.dto.CreditApplicationResponseDTO;
import com.sentinelbank.finguard.model.CreditApplication;
import com.sentinelbank.finguard.service.CreditApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing credit applications.
 */
@RestController
@RequestMapping("/api/credit-applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CreditApplicationController {

    private final CreditApplicationService creditApplicationService;

    /**
     * Creates a new credit application. Triggers the ML prediction engine.
     *
     * @param requestDTO Application request data and risk parameters
     * @return Response containing the created application details (201 Created)
     */
    @PostMapping
    public ResponseEntity<CreditApplicationResponseDTO> createApplication(@RequestBody CreditApplicationRequestDTO requestDTO) {
        CreditApplication application = creditApplicationService.createApplication(requestDTO);
        CreditApplicationResponseDTO responseDTO = toResponseDTO(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * Lists all credit applications.
     *
     * @return List of all credit application response DTOs
     */
    @GetMapping
    public ResponseEntity<List<CreditApplicationResponseDTO>> getAllApplications() {
        List<CreditApplication> applications = creditApplicationService.getAllApplications();
        List<CreditApplicationResponseDTO> responseDTOs = applications.stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Retrieves a credit application by ID.
     *
     * @param id Application ID
     * @return Credit application details
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreditApplicationResponseDTO> getApplicationById(@PathVariable Long id) {
        CreditApplication application = creditApplicationService.getApplicationById(id);
        CreditApplicationResponseDTO responseDTO = toResponseDTO(application);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Retrieves all credit applications belonging to a specific customer.
     *
     * @param customerId Customer ID
     * @return List of applications belonging to the customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CreditApplicationResponseDTO>> getApplicationsByCustomerId(@PathVariable Long customerId) {
        List<CreditApplication> applications = creditApplicationService.getApplicationsByCustomerId(customerId);
        List<CreditApplicationResponseDTO> responseDTOs = applications.stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Deletes a credit application by ID.
     *
     * @param id Application ID to be deleted
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        creditApplicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Maps CreditApplication entity to CreditApplicationResponseDTO.
     */
    private CreditApplicationResponseDTO toResponseDTO(CreditApplication application) {
        return CreditApplicationResponseDTO.builder()
            .id(application.getId())
            .customerId(application.getCustomer().getId())
            .customerName(application.getCustomer().getName())
            .requestedAmount(application.getRequestedAmount())
            .applicationDate(application.getApplicationDate())
            .status(application.getStatus())
            .entropyScore(application.getEntropyScore())
            .build();
    }
}
