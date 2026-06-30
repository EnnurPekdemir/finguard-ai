package com.sentinelbank.finguard.dto;

import lombok.*;

/**
 * Kredi başvurusu oluşturma isteği için kullanılan DTO.
 *
 * <p>İstemci müşteri ID'si, talep ettiği tutar ve model tahmini için
 * gerekli risk parametrelerini gönderir.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplicationRequestDTO {

    private Long customerId;
    private Double requestedAmount;

    // ML tahmin parametreleri
    private Integer age;
    private String homeOwnership;
    private Double employmentLength;
    private String loanIntent;
    private String loanGrade;
    private Double loanInterestRate;
    private String defaultOnFile;
    private Integer creditHistoryLength;
}
