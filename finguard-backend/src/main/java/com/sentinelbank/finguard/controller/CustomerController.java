package com.sentinelbank.finguard.controller;

import com.sentinelbank.finguard.dto.CreditApplicationResponseDTO;
import com.sentinelbank.finguard.dto.CustomerDTO;
import com.sentinelbank.finguard.dto.CustomerResponseDTO;
import com.sentinelbank.finguard.model.CreditApplication;
import com.sentinelbank.finguard.model.Customer;
import com.sentinelbank.finguard.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Müşteri yönetimi REST API controller'ı.
 *
 * <p>Tüm endpoint'ler {@code /api/customers} altında gruplanmıştır.
 * Entity sınıfları doğrudan dışarıya açılmaz — tüm istek ve yanıtlar
 * DTO'lar üzerinden yapılır.</p>
 *
 * <h3>Endpoint Tablosu:</h3>
 * <table>
 *   <tr><td>POST</td><td>/api/customers</td><td>Yeni müşteri oluştur</td></tr>
 *   <tr><td>GET</td><td>/api/customers</td><td>Tüm müşterileri listele</td></tr>
 *   <tr><td>GET</td><td>/api/customers/{id}</td><td>ID ile müşteri getir</td></tr>
 *   <tr><td>PUT</td><td>/api/customers/{id}</td><td>Müşteri güncelle</td></tr>
 *   <tr><td>DELETE</td><td>/api/customers/{id}</td><td>Müşteri sil</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // ─────────────────────────────────────────────
    //  POST /api/customers — Yeni müşteri oluştur
    // ─────────────────────────────────────────────

    /**
     * Yeni müşteri kaydı oluşturur.
     *
     * <p><strong>Postman Test:</strong></p>
     * <pre>
     * POST http://localhost:8081/api/customers
     * Content-Type: application/json
     *
     * {
     *   "name": "Ahmet Yılmaz",
     *   "identityNumber": "12345678901",
     *   "email": "ahmet.yilmaz@email.com",
     *   "monthlyIncome": 25000.0
     * }
     * </pre>
     *
     * @param customerDTO müşteri bilgilerini içeren DTO
     * @return oluşturulan müşterinin yanıt DTO'su (201 Created)
     */
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDTO customerDTO) {
        try {
            // DTO → Entity dönüşümü
            Customer customer = Customer.builder()
                .name(customerDTO.getName())
                .identityNumber(customerDTO.getIdentityNumber())
                .email(customerDTO.getEmail())
                .monthlyIncome(customerDTO.getMonthlyIncome())
                .build();

            Customer savedCustomer = customerService.createCustomer(customer);

            // Entity → Response DTO dönüşümü
            CustomerResponseDTO responseDTO = toResponseDTO(savedCustomer);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    // ─────────────────────────────────────────────
    //  GET /api/customers — Tüm müşterileri listele
    // ─────────────────────────────────────────────

    /**
     * Kayıtlı tüm müşterileri döndürür.
     *
     * <p><strong>Postman Test:</strong></p>
     * <pre>
     * GET http://localhost:8081/api/customers
     * </pre>
     *
     * @return müşteri listesi (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();

        List<CustomerResponseDTO> responseDTOs = customers.stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    // ─────────────────────────────────────────────
    //  GET /api/customers/{id} — ID ile müşteri getir
    // ─────────────────────────────────────────────

    /**
     * Belirli bir müşteriyi ID ile getirir.
     *
     * <p><strong>Postman Test:</strong></p>
     * <pre>
     * GET http://localhost:8081/api/customers/1
     * </pre>
     *
     * @param id müşteri ID'si
     * @return müşteri bilgisi (200 OK) veya hata mesajı (404 Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            Customer customer = customerService.getCustomerById(id);
            CustomerResponseDTO responseDTO = toResponseDTO(customer);
            return ResponseEntity.ok(responseDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    // ─────────────────────────────────────────────
    //  PUT /api/customers/{id} — Müşteri güncelle
    // ─────────────────────────────────────────────

    /**
     * Mevcut müşteriyi günceller.
     *
     * <p><strong>Postman Test:</strong></p>
     * <pre>
     * PUT http://localhost:8081/api/customers/1
     * Content-Type: application/json
     *
     * {
     *   "name": "Ahmet Yılmaz (Güncel)",
     *   "identityNumber": "12345678901",
     *   "email": "ahmet.yilmaz@yenimail.com",
     *   "monthlyIncome": 30000.0
     * }
     * </pre>
     *
     * @param id          güncellenecek müşterinin ID'si
     * @param customerDTO yeni müşteri bilgileri
     * @return güncellenmiş müşteri (200 OK) veya hata mesajı
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        try {
            Customer updatedData = Customer.builder()
                .name(customerDTO.getName())
                .identityNumber(customerDTO.getIdentityNumber())
                .email(customerDTO.getEmail())
                .monthlyIncome(customerDTO.getMonthlyIncome())
                .build();

            Customer updatedCustomer = customerService.updateCustomer(id, updatedData);
            CustomerResponseDTO responseDTO = toResponseDTO(updatedCustomer);

            return ResponseEntity.ok(responseDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    // ─────────────────────────────────────────────
    //  DELETE /api/customers/{id} — Müşteri sil
    // ─────────────────────────────────────────────

    /**
     * Müşteriyi ve bağlı tüm kredi başvurularını siler.
     *
     * <p><strong>Postman Test:</strong></p>
     * <pre>
     * DELETE http://localhost:8081/api/customers/1
     * </pre>
     *
     * @param id silinecek müşterinin ID'si
     * @return 204 No Content veya hata mesajı (404 Not Found)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", true,
                    "message", e.getMessage()
                ));
        }
    }

    // ─────────────────────────────────────────────
    //  Entity → DTO Dönüşüm Yardımcısı
    // ─────────────────────────────────────────────

    /**
     * {@link Customer} entity'sini {@link CustomerResponseDTO}'ya dönüştürür.
     *
     * <p>İlişkili kredi başvuruları da {@link CreditApplicationResponseDTO}
     * listesine çevrilir. Böylece Jackson sonsuz döngü sorunu (infinite recursion)
     * oluşmaz.</p>
     */
    private CustomerResponseDTO toResponseDTO(Customer customer) {
        List<CreditApplicationResponseDTO> applicationDTOs;

        // Lazy-loaded koleksiyonu güvenli şekilde dönüştür
        try {
            applicationDTOs = customer.getCreditApplications().stream()
                .map(this::toApplicationResponseDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            // Lazy initialization hatası durumunda boş liste döndür
            applicationDTOs = Collections.emptyList();
        }

        return CustomerResponseDTO.builder()
            .id(customer.getId())
            .name(customer.getName())
            .identityNumber(customer.getIdentityNumber())
            .email(customer.getEmail())
            .monthlyIncome(customer.getMonthlyIncome())
            .creditApplications(applicationDTOs)
            .build();
    }

    /**
     * {@link CreditApplication} entity'sini {@link CreditApplicationResponseDTO}'ya dönüştürür.
     */
    private CreditApplicationResponseDTO toApplicationResponseDTO(CreditApplication application) {
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
