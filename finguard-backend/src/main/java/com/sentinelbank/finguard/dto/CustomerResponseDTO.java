package com.sentinelbank.finguard.dto;

import lombok.*;

import java.util.List;

/**
 * Müşteri bilgisi yanıtı için kullanılan DTO.
 *
 * <p>Entity'nin iç yapısını gizler ve müşteriye ait kredi
 * başvurularını özetlenmiş biçimde döndürür.</p>
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
