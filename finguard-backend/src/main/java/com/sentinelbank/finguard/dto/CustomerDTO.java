package com.sentinelbank.finguard.dto;

import lombok.*;

/**
 * DTO representing customer creation and update requests.
 *
 * <p>Used instead of exposing the Customer entity directly to prevent security risks
 * and decouple API contracts from database schema definitions.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    private String name;
    private String identityNumber;
    private String email;
    private Double monthlyIncome;
}
