package com.sentinelbank.finguard.service;

import com.sentinelbank.finguard.model.ApplicationStatus;
import com.sentinelbank.finguard.model.CreditApplication;
import com.sentinelbank.finguard.model.Customer;
import com.sentinelbank.finguard.repository.CreditApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kredi başvurusu iş mantığı katmanı.
 *
 * <p>Başvuru oluşturma, sorgulama ve durum güncelleme operasyonlarını
 * içerir. İleride ML servis entegrasyonu bu katmana eklenecektir.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreditApplicationService {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CustomerService customerService;

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────

    /**
     * Yeni bir kredi başvurusu oluşturur.
     *
     * <p>Başvuru, belirtilen müşteriye bağlanır.
     * Başlangıç durumu {@link ApplicationStatus#PENDING} olarak atanır.
     * {@code applicationDate} alanı {@code @PrePersist} ile otomatik set edilir.</p>
     *
     * <p><strong>İleride:</strong> Bu metot, başvuru kaydedildikten sonra
     * FastAPI ML servisini çağırarak {@code entropyScore} ve nihai
     * {@code status} değerlerini otomatik olarak güncelleyecektir.</p>
     *
     * @param customerId      başvuruyu yapan müşterinin ID'si
     * @param requestedAmount talep edilen kredi tutarı (TL)
     * @return veritabanına kaydedilmiş başvuru (id ve applicationDate atanmış)
     * @throws IllegalArgumentException müşteri bulunamazsa veya tutar geçersizse
     */
    public CreditApplication createApplication(Long customerId, Double requestedAmount) {
        // Tutar doğrulama
        if (requestedAmount == null || requestedAmount <= 0) {
            throw new IllegalArgumentException(
                "Talep edilen kredi tutarı sıfırdan büyük olmalıdır."
            );
        }

        // Müşteriyi bul (yoksa exception fırlatır)
        Customer customer = customerService.getCustomerById(customerId);

        // Başvuruyu oluştur
        CreditApplication application = CreditApplication.builder()
            .customer(customer)
            .requestedAmount(requestedAmount)
            .status(ApplicationStatus.PENDING)
            .build();

        return creditApplicationRepository.save(application);
    }

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /**
     * ID'ye göre kredi başvurusu getirir.
     *
     * @param id başvuru ID'si
     * @return bulunan başvuru
     * @throws IllegalArgumentException başvuru bulunamazsa
     */
    @Transactional(readOnly = true)
    public CreditApplication getApplicationById(Long id) {
        return creditApplicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Kredi başvurusu bulunamadı. ID: " + id
            ));
    }

    /**
     * Tüm kredi başvurularını listeler.
     *
     * @return başvuru listesi
     */
    @Transactional(readOnly = true)
    public List<CreditApplication> getAllApplications() {
        return creditApplicationRepository.findAll();
    }

    /**
     * Belirli bir müşteriye ait tüm kredi başvurularını getirir.
     *
     * @param customerId müşteri ID'si
     * @return o müşteriye ait başvuru listesi
     */
    @Transactional(readOnly = true)
    public List<CreditApplication> getApplicationsByCustomerId(Long customerId) {
        // Müşterinin var olduğunu doğrula
        customerService.getCustomerById(customerId);
        return creditApplicationRepository.findByCustomerId(customerId);
    }

    /**
     * Belirli bir durumdaki tüm başvuruları listeler.
     * Örneğin: tüm PENDING veya MANUAL_REVIEW başvurularını getirmek için.
     *
     * @param status filtrelenecek durum
     * @return filtrelenmiş başvuru listesi
     */
    @Transactional(readOnly = true)
    public List<CreditApplication> getApplicationsByStatus(ApplicationStatus status) {
        return creditApplicationRepository.findByStatus(status);
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────

    /**
     * Başvurunun durumunu günceller.
     *
     * <p>Genellikle ML servisinden dönen sonuç sonrası veya
     * analist tarafından manuel inceleme tamamlandığında kullanılır.</p>
     *
     * @param id     başvuru ID'si
     * @param status yeni durum
     * @return güncellenmiş başvuru
     * @throws IllegalArgumentException başvuru bulunamazsa
     */
    public CreditApplication updateApplicationStatus(Long id, ApplicationStatus status) {
        CreditApplication application = getApplicationById(id);
        application.setStatus(status);
        return creditApplicationRepository.save(application);
    }

    /**
     * Başvurunun entropy skorunu ve durumunu birlikte günceller.
     *
     * <p>ML servisinden dönen sonuç sonrası çağrılması amaçlanmıştır.</p>
     *
     * @param id           başvuru ID'si
     * @param entropyScore Shannon Entropy skoru (0.0 – 1.0)
     * @param status       ML sonucuna göre belirlenen yeni durum
     * @return güncellenmiş başvuru
     * @throws IllegalArgumentException başvuru bulunamazsa
     */
    public CreditApplication updateApplicationWithMLResult(Long id, Double entropyScore, ApplicationStatus status) {
        CreditApplication application = getApplicationById(id);
        application.setEntropyScore(entropyScore);
        application.setStatus(status);
        return creditApplicationRepository.save(application);
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────

    /**
     * Kredi başvurusunu siler.
     *
     * @param id silinecek başvurunun ID'si
     * @throws IllegalArgumentException başvuru bulunamazsa
     */
    public void deleteApplication(Long id) {
        CreditApplication application = getApplicationById(id);
        creditApplicationRepository.delete(application);
    }
}
