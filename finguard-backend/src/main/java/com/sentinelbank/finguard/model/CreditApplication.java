package com.sentinelbank.finguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity class representing a credit application.
 *
 * <p>Each application belongs to a {@link Customer} ({@code @ManyToOne}).
 * The approval / rejection / manual review decision is made based on the
 * {@code entropyScore} returned after the application is sent to the FinGuard ML service.</p>
 *
 * <p>Table name: {@code credit_applications}</p>
 */
@Entity
@Table(name = "credit_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplication {

    /**
     * Primary key – Auto-increment (AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────
    //  Relationship: Each application belongs to a customer
    // ─────────────────────────────────────────────

    /**
     * The customer who made this application.
     *
     * <p>{@code @JoinColumn(name = "customer_id")} → Adds a {@code customer_id} FK column
     * to the {@code credit_applications} table in the database.</p>
     *
     * <p>{@code nullable = false} → An application must have a customer.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Requested loan amount (TL).
     */
    @Column(nullable = false)
    private Double requestedAmount;

    /**
     * Application date and time.
     * Automatically set using {@code @PrePersist}.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime applicationDate;

    /**
     * Current status of the application.
     *
     * @see ApplicationStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /**
     * Shannon Entropy score returned from the FinGuard ML service.
     *
     * <p>Ranges between 0.0 (certain decision) and 1.0 (complete uncertainty).
     * If {@code null}, the ML service has not been queried yet.</p>
     */
    @Column
    private Double entropyScore;

    // ─────────────────────────────────────────────
    //  JPA Lifecycle Callback
    // ─────────────────────────────────────────────

    /**
     * Automatically sets the application date to current date/time
     * before the entity is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        this.applicationDate = LocalDateTime.now();
    }
}
