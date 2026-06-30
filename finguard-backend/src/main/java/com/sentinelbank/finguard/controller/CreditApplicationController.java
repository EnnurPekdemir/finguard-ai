package com.sentinelbank.finguard.controller;

import com.sentinelbank.finguard.dto.CreditApplicationRequestDTO;
import com.sentinelbank.finguard.dto.CreditApplicationResponseDTO;
import com.sentinelbank.finguard.model.CreditApplication;
import com.sentinelbank.finguard.service.CreditApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kredi başvuruları REST API controller'ı.
 */
@RestController
@RequestMapping("/api/credit-applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CreditApplicationController {

    private final CreditApplicationService creditApplicationService;

    /**
     * Yeni kredi başvurusu oluşturur. ML tahmin motoru tetiklenir ve sonuç kaydedilir.
     *
     * @param requestDTO başvuru verileri ve ML parametreleri
     * @return oluşturulan başvuru bilgileri (201 Created)
     */
    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody CreditApplicationRequestDTO requestDTO) {
        try {
            CreditApplication application = creditApplicationService.createApplication(requestDTO);
            CreditApplicationResponseDTO responseDTO = toResponseDTO(application);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * Tüm kredi başvurularını listeler.
     *
     * @return tüm başvurular listesi (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<CreditApplicationResponseDTO>> getAllApplications() {
        List<CreditApplication> applications = creditApplicationService.getAllApplications();
        List<CreditApplicationResponseDTO> responseDTOs = applications.stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    /**
     * ID ile kredi başvurusu getirir.
     *
     * @param id başvuru ID'si
     * @return başvuru detayı (200 OK) veya hata (404 Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable Long id) {
        try {
            CreditApplication application = creditApplicationService.getApplicationById(id);
            CreditApplicationResponseDTO responseDTO = toResponseDTO(application);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * Belirli bir müşteriye ait tüm kredi başvurularını getirir.
     *
     * @param customerId müşteri ID'si
     * @return müşteriye ait başvurular listesi (200 OK)
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getApplicationsByCustomerId(@PathVariable Long customerId) {
        try {
            List<CreditApplication> applications = creditApplicationService.getApplicationsByCustomerId(customerId);
            List<CreditApplicationResponseDTO> responseDTOs = applications.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * ID ile kredi başvurusu siler.
     *
     * @param id silinecek başvuru ID'si
     * @return 204 No Content veya hata (404 Not Found)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id) {
        try {
            creditApplicationService.deleteApplication(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * Entity -> DTO dönüşümü yapar.
     */
    private CreditApplicationResponseDTO toResponseDTO(CreditApplication application) {
        return CreditApplicationResponseDTO.builder()
            .id(application.getId())
            .customerId(application.getCustomer().getId())
            .customerName(application.getCustomer().getName())
            .requestedAmount(application.getRequestedAmount())
            .applicationDate(application.getApplicationDate())
            .status(application.getStatus())
            .entropyScore(application.getEntropyScore())
            .build();
    }
}
