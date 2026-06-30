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
 * REST Client Service communicating with the FastAPI ML service.
 */
@Service
public class MLClientService {

    @Value("${finguard.ml.url:http://localhost:8000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends the credit application risk parameters to FastAPI /predict endpoint
     * and returns the ML model prediction results.
     *
     * @param request  Credit application request containing risk parameters
     * @param customer Customer who makes the application
     * @return ML prediction response DTO
     */
    public MLPredictionResponseDTO predictCreditRisk(CreditApplicationRequestDTO request, Customer customer) {
        String url = mlServiceUrl + "/predict";

        // Map fields to match FastAPI input properties
        Map<String, Object> payload = new HashMap<>();

        // Age parameter (default: 30)
        payload.put("person_age", request.getAge() != null ? request.getAge() : 30);

        // Annual Income (default: customer monthly income * 12)
        double annualIncome = (customer.getMonthlyIncome() != null) ? customer.getMonthlyIncome() * 12 : 60000.0;
        payload.put("person_income", (int) annualIncome);

        // Home ownership (default: RENT)
        payload.put("person_home_ownership", request.getHomeOwnership() != null ? request.getHomeOwnership().toUpperCase() : "RENT");

        // Employment length (default: 4.0 years)
        payload.put("person_emp_length", request.getEmploymentLength() != null ? request.getEmploymentLength() : 4.0);

        // Loan intent (default: PERSONAL)
        payload.put("loan_intent", request.getLoanIntent() != null ? request.getLoanIntent().toUpperCase() : "PERSONAL");

        // Loan grade (default: C)
        payload.put("loan_grade", request.getLoanGrade() != null ? request.getLoanGrade().toUpperCase() : "C");

        // Requested loan amount (default: requested amount)
        double requestedAmount = (request.getRequestedAmount() != null) ? request.getRequestedAmount() : 10000.0;
        payload.put("loan_amnt", (int) requestedAmount);

        // Loan interest rate (default: 11.0%)
        payload.put("loan_int_rate", request.getLoanInterestRate() != null ? request.getLoanInterestRate() : 11.0);

        // Default on file (default: N)
        payload.put("cb_person_default_on_file", request.getDefaultOnFile() != null ? request.getDefaultOnFile().toUpperCase() : "N");

        // Credit history length (default: 3 years)
        payload.put("cb_person_cred_hist_length", request.getCreditHistoryLength() != null ? request.getCreditHistoryLength() : 3);

        try {
            return restTemplate.postForObject(url, payload, MLPredictionResponseDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during ML prediction calculation: " + e.getMessage(), e);
        }
    }
}
