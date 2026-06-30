package com.sentinelbank.finguard.service;

import com.sentinelbank.finguard.dto.CreditApplicationRequestDTO;
import com.sentinelbank.finguard.dto.MLPredictionResponseDTO;
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
 * içerir. ML servis entegrasyonu bu katmanda yer almaktadır.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreditApplicationService {

    private final CreditApplicationRepository creditApplicationRepository;
    private final CustomerService customerService;
    private final MLClientService mlClientService;

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────

    /**
     * Yeni bir kredi başvurusu oluşturur ve ML servisine sorgular.
     *
     * @param request kredi başvuru verileri ve risk parametrelerini içeren DTO
     * @return veritabanına kaydedilmiş ve ML sonucuna göre güncellenmiş başvuru
     */
    public CreditApplication createApplication(CreditApplicationRequestDTO request) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Müşteri ID'si boş olamaz.");
        }
        if (request.getRequestedAmount() == null || request.getRequestedAmount() <= 0) {
            throw new IllegalArgumentException("Talep edilen kredi tutarı sıfırdan büyük olmalıdır.");
        }

        // Müşteriyi bul (yoksa exception fırlatır)
        Customer customer = customerService.getCustomerById(request.getCustomerId());

        // Başvuruyu ilk olarak PENDING olarak oluştur
        CreditApplication application = CreditApplication.builder()
            .customer(customer)
            .requestedAmount(request.getRequestedAmount())
            .status(ApplicationStatus.PENDING)
            .build();

        // İlk kaydı yap (ID alabilmek için)
        application = creditApplicationRepository.save(application);

        // FastAPI ML servisini çağırarak risk skorunu hesapla
        try {
            MLPredictionResponseDTO prediction = mlClientService.predictCreditRisk(request, customer);

            // Gelen sonuca göre başvuruyu güncelle
            application.setEntropyScore(prediction.getEntropyScore());

            String decisionFlow = prediction.getDecisionFlow();
            if ("SISTEM_ONAY".equalsIgnoreCase(decisionFlow)) {
                application.setStatus(ApplicationStatus.APPROVED);
            } else if ("SISTEM_RED".equalsIgnoreCase(decisionFlow)) {
                application.setStatus(ApplicationStatus.REJECTED);
            } else if ("MANUEL_INCELEME".equalsIgnoreCase(decisionFlow)) {
                application.setStatus(ApplicationStatus.MANUAL_REVIEW);
            } else {
                // Fallback: prediction flag'ine göre karar ver
                if (prediction.getPrediction() != null && prediction.getPrediction() == 1) {
                    application.setStatus(ApplicationStatus.REJECTED);
                } else {
                    application.setStatus(ApplicationStatus.APPROVED);
                }
            }

            // Güncellenmiş başvuruyu kaydet
            application = creditApplicationRepository.save(application);

        } catch (Exception e) {
            throw new RuntimeException("Kredi başvurusu değerlendirilirken risk analiz motoruna bağlanılamadı: " + e.getMessage(), e);
        }

        return application;
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
