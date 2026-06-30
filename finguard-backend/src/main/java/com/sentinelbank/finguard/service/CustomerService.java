package com.sentinelbank.finguard.service;

import com.sentinelbank.finguard.exception.CustomerNotFoundException;
import com.sentinelbank.finguard.model.Customer;
import com.sentinelbank.finguard.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Customer business logic service layer.
 *
 * <p>Contains basic CRUD operations and business rules (e.g., unique identity check).</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────

    /**
     * Creates a new customer profile.
     *
     * @param customer Customer entity to be saved
     * @return Saved customer entity with generated ID
     * @throws IllegalArgumentException if the national identity or email is already registered
     */
    public Customer createCustomer(Customer customer) {
        // Unique national identity check
        if (customerRepository.existsByIdentityNumber(customer.getIdentityNumber())) {
            throw new IllegalArgumentException(
                "Customer with this National Identity Number is already registered: "
                    + customer.getIdentityNumber()
            );
        }

        // Unique email check
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException(
                "Customer with this email address is already registered: "
                    + customer.getEmail()
            );
        }

        return customerRepository.save(customer);
    }

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /**
     * Retrieves a customer by ID.
     *
     * @param id Customer ID
     * @return Found customer entity
     * @throws CustomerNotFoundException if the customer does not exist
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(
                "Customer not found. ID: " + id
            ));
    }

    /**
     * Retrieves a customer by National Identity Number.
     *
     * @param identityNumber 11-digit national identity number
     * @return Found customer entity
     * @throws CustomerNotFoundException if the customer does not exist
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByIdentityNumber(String identityNumber) {
        return customerRepository.findByIdentityNumber(identityNumber)
            .orElseThrow(() -> new CustomerNotFoundException(
                "Customer not found with National Identity Number: " + identityNumber
            ));
    }

    /**
     * Retrieves all registered customers.
     *
     * @return List of all customers
     */
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────

    /**
     * Updates an existing customer profile.
     *
     * @param id              Customer ID to be updated
     * @param updatedCustomer New customer data
     * @return Updated customer entity
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws IllegalArgumentException if the updated identity number or email conflicts with an existing customer
     */
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existingCustomer = getCustomerById(id);

        // Identity number changed -> unique check
        if (!existingCustomer.getIdentityNumber().equals(updatedCustomer.getIdentityNumber())
                && customerRepository.existsByIdentityNumber(updatedCustomer.getIdentityNumber())) {
            throw new IllegalArgumentException(
                "Another customer is already registered with this National Identity Number: "
                    + updatedCustomer.getIdentityNumber()
            );
        }

        // Email changed -> unique check
        if (!existingCustomer.getEmail().equals(updatedCustomer.getEmail())
                && customerRepository.existsByEmail(updatedCustomer.getEmail())) {
            throw new IllegalArgumentException(
                "Another customer is already registered with this email address: "
                    + updatedCustomer.getEmail()
            );
        }

        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setIdentityNumber(updatedCustomer.getIdentityNumber());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setMonthlyIncome(updatedCustomer.getMonthlyIncome());

        return customerRepository.save(existingCustomer);
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────

    /**
     * Deletes a customer and all their associated credit applications.
     *
     * @param id Customer ID to be deleted
     * @throws CustomerNotFoundException if the customer does not exist
     */
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }
}
