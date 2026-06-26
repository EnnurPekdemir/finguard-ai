package com.sentinelbank.finguard.repository;

import com.sentinelbank.finguard.model.ApplicationStatus;
import com.sentinelbank.finguard.model.CreditApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Kredi başvurusu entity'si için veritabanı erişim katmanı.
 *
 * <p>{@link JpaRepository} sayesinde temel CRUD operasyonları
 * (save, findById, findAll, deleteById, vb.) otomatik olarak sağlanır.</p>
 */
@Repository
public interface CreditApplicationRepository extends JpaRepository<CreditApplication, Long> {

    /**
     * Belirli bir müşteriye ait tüm kredi başvurularını getirir.
     *
     * @param customerId müşteri ID'si
     * @return başvuru listesi (boş olabilir)
     */
    List<CreditApplication> findByCustomerId(Long customerId);

    /**
     * Belirli bir duruma sahip tüm başvuruları getirir.
     * Örneğin: tüm {@code PENDING} veya {@code MANUAL_REVIEW} başvuruları.
     *
     * @param status başvuru durumu
     * @return başvuru listesi
     */
    List<CreditApplication> findByStatus(ApplicationStatus status);

    /**
     * Bir müşteriye ait, belirli durumdaki başvuruları getirir.
     *
     * @param customerId müşteri ID'si
     * @param status     başvuru durumu
     * @return filtrelenmiş başvuru listesi
     */
    List<CreditApplication> findByCustomerIdAndStatus(Long customerId, ApplicationStatus status);
}
