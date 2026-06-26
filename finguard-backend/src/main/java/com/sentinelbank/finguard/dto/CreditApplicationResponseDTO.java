package com.sentinelbank.finguard.dto;

import com.sentinelbank.finguard.model.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Kredi başvurusu yanıtı için kullanılan DTO.
 *
 * <p>Entity'deki {@code Customer} referansı yerine sadece
 * {@code customerId} ve {@code customerName} döndürülür.
 * Böylece sonsuz döngü (infinite recursion) riski ortadan kalkar
 * ve yanıt boyutu küçülür.</p>
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
