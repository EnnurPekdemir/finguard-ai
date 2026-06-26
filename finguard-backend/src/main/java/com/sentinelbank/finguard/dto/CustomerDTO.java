package com.sentinelbank.finguard.dto;

import lombok.*;

/**
 * Müşteri oluşturma ve güncelleme istekleri için kullanılan DTO.
 *
 * <p>Entity sınıfını ({@code Customer}) doğrudan API'ye açmak yerine
 * bu DTO kullanılır. Böylece:</p>
 * <ul>
 *   <li>İç veri modeli dış dünyadan gizlenir (güvenlik)</li>
 *   <li>API kontratı, entity'den bağımsız olarak versiyonlanabilir</li>
 *   <li>İstemciye gereksiz alanlar (id, creditApplications listesi) gönderilmez</li>
 * </ul>
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
