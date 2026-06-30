package com.sentinelbank.finguard.dto;

import com.sentinelbank.finguard.model.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO representing a credit application response.
 *
 * <p>Contains basic application information including status and entropy score.
 * Instead of the full customer object, only ID and name are included to avoid infinite recursion.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplicationResponseDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private Double requestedAmount;
    private LocalDateTime applicationDate;
    private ApplicationStatus status;
    private Double entropyScore;
}
