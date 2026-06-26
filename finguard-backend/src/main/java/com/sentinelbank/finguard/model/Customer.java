package com.sentinelbank.finguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Banka müşterisini temsil eden JPA entity sınıfı.
 *
 * <p>Her müşterinin benzersiz bir T.C. kimlik numarası ({@code identityNumber})
 * vardır ve birden fazla {@link CreditApplication} kaydına sahip olabilir.</p>
 *
 * <p>Tablo adı: {@code customers}</p>
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "identityNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    /**
     * Birincil anahtar – Otomatik artan (AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Müşterinin adı ve soyadı.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * T.C. Kimlik Numarası (11 hane, benzersiz).
     */
    @Column(nullable = false, unique = true, length = 11)
    private String identityNumber;

    /**
     * E-posta adresi.
     */
    @Column(nullable = false, length = 150)
    private String email;

    /**
     * Aylık gelir (TL).
     */
    @Column(nullable = false)
    private Double monthlyIncome;

    // ─────────────────────────────────────────────
    //  İlişki: Bir müşterinin birden fazla kredi başvurusu olabilir
    // ─────────────────────────────────────────────

    /**
     * Bu müşteriye ait tüm kredi başvuruları.
     *
     * <p>{@code mappedBy = "customer"} → İlişkinin sahibi
     * {@link CreditApplication#customer} alanıdır.</p>
     *
     * <p>{@code cascade = ALL} → Müşteri silindiğinde başvuruları da silinir.</p>
     *
     * <p>{@code orphanRemoval = true} → Listeden çıkarılan başvuru veritabanından da silinir.</p>
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CreditApplication> creditApplications = new ArrayList<>();

    // ─────────────────────────────────────────────
    //  Yardımcı metotlar (Bidirectional sync)
    // ─────────────────────────────────────────────

    /**
     * Müşteriye yeni bir kredi başvurusu ekler ve
     * başvurunun {@code customer} referansını otomatik olarak ayarlar.
     */
    public void addCreditApplication(CreditApplication application) {
        creditApplications.add(application);
        application.setCustomer(this);
    }

    /**
     * Müşteriden bir kredi başvurusunu kaldırır ve
     * başvurunun {@code customer} referansını null yapar.
     */
    public void removeCreditApplication(CreditApplication application) {
        creditApplications.remove(application);
        application.setCustomer(null);
    }
}
