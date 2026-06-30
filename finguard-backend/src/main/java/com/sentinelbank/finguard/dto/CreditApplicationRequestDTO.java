package com.sentinelbank.finguard.dto;

import lombok.*;

/**
 * DTO representing a request to create a credit application.
 *
 * <p>The client sends the customer ID, requested loan amount, and risk parameters
 * required for the ML prediction.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplicationRequestDTO {

    private Long customerId;
    private Double requestedAmount;

    // ML prediction parameters
    private Integer age;
    private String homeOwnership;
    private Double employmentLength;
    private String loanIntent;
    private String loanGrade;
    private Double loanInterestRate;
    private String defaultOnFile;
    private Integer creditHistoryLength;
}
