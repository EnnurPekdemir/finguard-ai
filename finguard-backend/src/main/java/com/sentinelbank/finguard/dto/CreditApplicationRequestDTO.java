package com.sentinelbank.finguard.dto;

import lombok.*;

/**
 * Kredi başvurusu oluşturma isteği için kullanılan DTO.
 *
 * <p>İstemci sadece müşteri ID'si ve talep ettiği tutarı gönderir.
 * Geri kalan alanlar (applicationDate, status, entropyScore) sunucu
 * tarafından otomatik olarak atanır.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplicationRequestDTO {

    private Long customerId;
    private Double requestedAmount;
}
