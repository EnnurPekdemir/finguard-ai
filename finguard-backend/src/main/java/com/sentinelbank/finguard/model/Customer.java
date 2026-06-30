package com.sentinelbank.finguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity class representing a bank customer.
 *
 * <p>Each customer has a unique identity number ({@code identityNumber})
 * and can have multiple {@link CreditApplication} records.</p>
 *
 * <p>Table name: {@code customers}</p>
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "identityNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    /**
     * Primary key – Auto-increment (AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer's full name.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * National Identity Number (11 digits, unique).
     */
    @Column(nullable = false, unique = true, length = 11)
    private String identityNumber;

    /**
     * Email address.
     */
    @Column(nullable = false, length = 150)
    private String email;

    /**
     * Monthly income (TL).
     */
    @Column(nullable = false)
    private Double monthlyIncome;

    // ─────────────────────────────────────────────
    //  Relationship: A customer can have multiple credit applications
    // ─────────────────────────────────────────────

    /**
     * All credit applications belonging to this customer.
     *
     * <p>{@code mappedBy = "customer"} → The owner of the relationship
     * is the {@link CreditApplication#customer} field.</p>
     *
     * <p>{@code cascade = ALL} → If the customer is deleted, their applications are also deleted.</p>
     *
     * <p>{@code orphanRemoval = true} → If an application is removed from the list, it is also deleted from the database.</p>
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CreditApplication> creditApplications = new ArrayList<>();

    // ─────────────────────────────────────────────
    //  Helper methods (Bidirectional sync)
    // ─────────────────────────────────────────────

    /**
     * Adds a new credit application to the customer and
     * automatically sets the {@code customer} reference on the application.
     */
    public void addCreditApplication(CreditApplication application) {
        creditApplications.add(application);
        application.setCustomer(this);
    }

    /**
     * Removes a credit application from the customer and
     * sets the {@code customer} reference on the application to null.
     */
    public void removeCreditApplication(CreditApplication application) {
        creditApplications.remove(application);
        application.setCustomer(null);
    }
}
