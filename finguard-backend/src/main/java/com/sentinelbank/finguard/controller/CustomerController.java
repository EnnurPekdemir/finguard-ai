package com.sentinelbank.finguard.controller;

import com.sentinelbank.finguard.dto.CreditApplicationResponseDTO;
import com.sentinelbank.finguard.dto.CustomerDTO;
import com.sentinelbank.finguard.dto.CustomerResponseDTO;
import com.sentinelbank.finguard.model.CreditApplication;
import com.sentinelbank.finguard.model.Customer;
import com.sentinelbank.finguard.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing customer accounts.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Creates a new customer profile.
     *
     * @param customerDTO DTO containing customer details
     * @return Response containing the created customer details (201 Created)
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        Customer customer = Customer.builder()
            .name(customerDTO.getName())
            .identityNumber(customerDTO.getIdentityNumber())
            .email(customerDTO.getEmail())
            .monthlyIncome(customerDTO.getMonthlyIncome())
            .build();

        Customer savedCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(savedCustomer));
    }

    /**
     * Lists all registered customers.
     *
     * @return List of all customer response DTOs
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        List<CustomerResponseDTO> responseDTOs = customers.stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * Retrieves a specific customer by ID.
     *
     * @param id Customer ID
     * @return Customer details
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(toResponseDTO(customer));
    }

    /**
     * Updates an existing customer profile.
     *
     * @param id          Customer ID to be updated
     * @param customerDTO Updated customer details
     * @return Updated customer details
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        Customer updatedData = Customer.builder()
            .name(customerDTO.getName())
            .identityNumber(customerDTO.getIdentityNumber())
            .email(customerDTO.getEmail())
            .monthlyIncome(customerDTO.getMonthlyIncome())
            .build();

        Customer updatedCustomer = customerService.updateCustomer(id, updatedData);
        return ResponseEntity.ok(toResponseDTO(updatedCustomer));
    }

    /**
     * Deletes a customer and all their credit applications.
     *
     * @param id Customer ID to be deleted
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Maps Customer entity to CustomerResponseDTO.
     */
    private CustomerResponseDTO toResponseDTO(Customer customer) {
        List<CreditApplicationResponseDTO> applicationDTOs;

        try {
            applicationDTOs = customer.getCreditApplications().stream()
                .map(this::toApplicationResponseDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            applicationDTOs = Collections.emptyList();
        }

        return CustomerResponseDTO.builder()
            .id(customer.getId())
            .name(customer.getName())
            .identityNumber(customer.getIdentityNumber())
            .email(customer.getEmail())
            .monthlyIncome(customer.getMonthlyIncome())
            .creditApplications(applicationDTOs)
            .build();
    }

    /**
     * Maps CreditApplication entity to CreditApplicationResponseDTO.
     */
    private CreditApplicationResponseDTO toApplicationResponseDTO(CreditApplication application) {
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
