package com.sentinelbank.finguard.dto;

import lombok.*;

import java.util.List;

/**
 * DTO representing customer profile response details.
 *
 * <p>Decouples database structure from API response and contains simplified
 * credit applications list to avoid circular dependencies.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String identityNumber;
    private String email;
    private Double monthlyIncome;
    private List<CreditApplicationResponseDTO> creditApplications;
}
