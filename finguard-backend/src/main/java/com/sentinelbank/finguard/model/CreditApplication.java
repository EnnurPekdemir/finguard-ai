package com.sentinelbank.finguard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Kredi başvurusunu temsil eden JPA entity sınıfı.
 *
 * <p>Her başvuru bir {@link Customer} müşterisine aittir ({@code @ManyToOne}).
 * Başvuru, FinGuard ML servisine gönderildikten sonra dönen
 * {@code entropyScore} değerine göre onay / ret / manuel inceleme kararı alınır.</p>
 *
 * <p>Tablo adı: {@code credit_applications}</p>
 */
@Entity
@Table(name = "credit_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplication {

    /**
     * Birincil anahtar – Otomatik artan (AUTO_INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────
    //  İlişki: Her başvuru bir müşteriye aittir
    // ─────────────────────────────────────────────

    /**
     * Bu başvuruyu yapan müşteri.
     *
     * <p>{@code @JoinColumn(name = "customer_id")} → Veritabanında
     * {@code credit_applications} tablosuna {@code customer_id} FK sütunu eklenir.</p>
     *
     * <p>{@code nullable = false} → Müşterisi olmayan başvuru olamaz.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Talep edilen kredi tutarı (TL).
     */
    @Column(nullable = false)
    private Double requestedAmount;

    /**
     * Başvuru tarihi ve saati.
     * {@code @PrePersist} ile otomatik olarak set edilir.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime applicationDate;

    /**
     * Başvurunun mevcut durumu.
     *
     * @see ApplicationStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /**
     * FinGuard ML servisinden dönen Shannon Entropy skoru.
     *
     * <p>0.0 (kesin karar) – 1.0 (tam belirsizlik) arasında değer alır.
     * {@code null} ise henüz ML servisi çağrılmamıştır.</p>
     */
    @Column
    private Double entropyScore;

    // ─────────────────────────────────────────────
    //  JPA Lifecycle Callback
    // ─────────────────────────────────────────────

    /**
     * Entity veritabanına ilk kez kaydedilmeden önce
     * başvuru tarihini otomatik olarak şimdiki zamana ayarlar.
     */
    @PrePersist
    protected void onCreate() {
        this.applicationDate = LocalDateTime.now();
    }
}
