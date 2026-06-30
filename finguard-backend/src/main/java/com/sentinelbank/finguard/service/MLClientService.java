package com.sentinelbank.finguard.service;

import com.sentinelbank.finguard.dto.CreditApplicationRequestDTO;
import com.sentinelbank.finguard.dto.MLPredictionResponseDTO;
import com.sentinelbank.finguard.model.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * FastAPI ML servisi ile iletişim kuran client servis.
 */
@Service
public class MLClientService {

    @Value("${finguard.ml.url:http://localhost:8000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * FastAPI /predict endpoint'ine kredi başvuru verilerini gönderir
     * ve ML model tahminini döndürür.
     *
     * @param request  kredi başvuru isteği risk parametreleri
     * @param customer başvuruyu yapan müşteri
     * @return modelden dönen tahmin sonucu DTO'su
     */
    public MLPredictionResponseDTO predictCreditRisk(CreditApplicationRequestDTO request, Customer customer) {
        String url = mlServiceUrl + "/predict";

        // FastAPI'nin beklediği alan adlarıyla eşleyelim
        Map<String, Object> payload = new HashMap<>();

        // Yaş parametresi (varsayılan: 30)
        payload.put("person_age", request.getAge() != null ? request.getAge() : 30);

        // Yıllık Gelir (varsayılan: müşterinin aylık geliri * 12)
        double annualIncome = (customer.getMonthlyIncome() != null) ? customer.getMonthlyIncome() * 12 : 60000.0;
        payload.put("person_income", (int) annualIncome);

        // Ev sahipliği (varsayılan: RENT)
        payload.put("person_home_ownership", request.getHomeOwnership() != null ? request.getHomeOwnership().toUpperCase() : "RENT");

        // Çalışma süresi (varsayılan: 4.0 yıl)
        payload.put("person_emp_length", request.getEmploymentLength() != null ? request.getEmploymentLength() : 4.0);

        // Kredi amacı (varsayılan: PERSONAL)
        payload.put("loan_intent", request.getLoanIntent() != null ? request.getLoanIntent().toUpperCase() : "PERSONAL");

        // Kredi notu derecesi (varsayılan: C)
        payload.put("loan_grade", request.getLoanGrade() != null ? request.getLoanGrade().toUpperCase() : "C");

        // Talep edilen miktar (varsayılan: talep edilen tutar)
        double requestedAmount = (request.getRequestedAmount() != null) ? request.getRequestedAmount() : 10000.0;
        payload.put("loan_amnt", (int) requestedAmount);

        // Kredi faiz oranı (varsayılan: 11.0%)
        payload.put("loan_int_rate", request.getLoanInterestRate() != null ? request.getLoanInterestRate() : 11.0);

        // Geçmiş ihlal kaydı (varsayılan: N)
        payload.put("cb_person_default_on_file", request.getDefaultOnFile() != null ? request.getDefaultOnFile().toUpperCase() : "N");

        // Kredi sicil geçmişi uzunluğu (varsayılan: 3 yıl)
        payload.put("cb_person_cred_hist_length", request.getCreditHistoryLength() != null ? request.getCreditHistoryLength() : 3);

        try {
            return restTemplate.postForObject(url, payload, MLPredictionResponseDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("ML tahmini hesaplanırken bir hata oluştu: " + e.getMessage(), e);
        }
    }
}
