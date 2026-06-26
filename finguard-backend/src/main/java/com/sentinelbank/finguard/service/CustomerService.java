package com.sentinelbank.finguard.service;

import com.sentinelbank.finguard.model.Customer;
import com.sentinelbank.finguard.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Müşteri iş mantığı katmanı.
 *
 * <p>Temel CRUD operasyonlarını ve iş kurallarını
 * (kimlik numarası / e-posta tekil kontrolü vb.) içerir.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────

    /**
     * Yeni müşteri kaydı oluşturur.
     *
     * <p>Aynı T.C. kimlik numarası veya e-posta adresiyle
     * daha önce kayıt yapılmışsa {@link IllegalArgumentException} fırlatır.</p>
     *
     * @param customer kaydedilecek müşteri nesnesi
     * @return veritabanına kaydedilmiş müşteri (id atanmış hâli)
     * @throws IllegalArgumentException kimlik numarası veya e-posta zaten mevcutsa
     */
    public Customer createCustomer(Customer customer) {
        // T.C. kimlik numarası benzersizlik kontrolü
        if (customerRepository.existsByIdentityNumber(customer.getIdentityNumber())) {
            throw new IllegalArgumentException(
                "Bu T.C. Kimlik Numarası ile kayıtlı bir müşteri zaten mevcut: "
                    + customer.getIdentityNumber()
            );
        }

        // E-posta benzersizlik kontrolü
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException(
                "Bu e-posta adresi ile kayıtlı bir müşteri zaten mevcut: "
                    + customer.getEmail()
            );
        }

        return customerRepository.save(customer);
    }

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /**
     * ID'ye göre müşteri getirir.
     *
     * @param id müşteri ID'si
     * @return bulunan müşteri
     * @throws IllegalArgumentException müşteri bulunamazsa
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Müşteri bulunamadı. ID: " + id
            ));
    }

    /**
     * T.C. Kimlik Numarasına göre müşteri getirir.
     *
     * @param identityNumber 11 haneli T.C. kimlik numarası
     * @return bulunan müşteri
     * @throws IllegalArgumentException müşteri bulunamazsa
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByIdentityNumber(String identityNumber) {
        return customerRepository.findByIdentityNumber(identityNumber)
            .orElseThrow(() -> new IllegalArgumentException(
                "Bu T.C. Kimlik Numarası ile kayıtlı müşteri bulunamadı: " + identityNumber
            ));
    }

    /**
     * Tüm müşterileri listeler.
     *
     * @return müşteri listesi
     */
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────

    /**
     * Mevcut müşteriyi günceller.
     *
     * <p>Güncellenecek müşterinin veritabanında mevcut olması gerekir.
     * Kimlik numarası veya e-posta değiştiriliyorsa tekil kontrolü yapılır.</p>
     *
     * @param id              güncellenecek müşterinin ID'si
     * @param updatedCustomer yeni bilgileri içeren müşteri nesnesi
     * @return güncellenmiş müşteri
     * @throws IllegalArgumentException müşteri bulunamazsa veya tekil kısıt ihlali varsa
     */
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existingCustomer = getCustomerById(id);

        // Kimlik numarası değişiyorsa → benzersizlik kontrolü
        if (!existingCustomer.getIdentityNumber().equals(updatedCustomer.getIdentityNumber())
                && customerRepository.existsByIdentityNumber(updatedCustomer.getIdentityNumber())) {
            throw new IllegalArgumentException(
                "Bu T.C. Kimlik Numarası ile kayıtlı başka bir müşteri zaten mevcut: "
                    + updatedCustomer.getIdentityNumber()
            );
        }

        // E-posta değişiyorsa → benzersizlik kontrolü
        if (!existingCustomer.getEmail().equals(updatedCustomer.getEmail())
                && customerRepository.existsByEmail(updatedCustomer.getEmail())) {
            throw new IllegalArgumentException(
                "Bu e-posta adresi ile kayıtlı başka bir müşteri zaten mevcut: "
                    + updatedCustomer.getEmail()
            );
        }

        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setIdentityNumber(updatedCustomer.getIdentityNumber());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setMonthlyIncome(updatedCustomer.getMonthlyIncome());

        return customerRepository.save(existingCustomer);
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────

    /**
     * Müşteriyi ve bağlı tüm kredi başvurularını siler.
     *
     * <p>{@code CascadeType.ALL} ve {@code orphanRemoval = true}
     * sayesinde ilişkili {@code CreditApplication} kayıtları da otomatik silinir.</p>
     *
     * @param id silinecek müşterinin ID'si
     * @throws IllegalArgumentException müşteri bulunamazsa
     */
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }
}
