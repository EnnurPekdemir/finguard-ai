package com.sentinelbank.finguard.repository;

import com.sentinelbank.finguard.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Müşteri entity'si için veritabanı erişim katmanı.
 *
 * <p>{@link JpaRepository} sayesinde temel CRUD operasyonları
 * (save, findById, findAll, deleteById, vb.) otomatik olarak sağlanır.</p>
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * T.C. Kimlik Numarasına göre müşteri arar.
     *
     * @param identityNumber 11 haneli T.C. kimlik numarası
     * @return bulunan müşteri veya {@link Optional#empty()}
     */
    Optional<Customer> findByIdentityNumber(String identityNumber);

    /**
     * Verilen T.C. Kimlik Numarasının veritabanında kayıtlı olup olmadığını kontrol eder.
     *
     * @param identityNumber 11 haneli T.C. kimlik numarası
     * @return varsa {@code true}, yoksa {@code false}
     */
    boolean existsByIdentityNumber(String identityNumber);

    /**
     * E-posta adresine göre müşteri arar.
     *
     * @param email müşteri e-posta adresi
     * @return bulunan müşteri veya {@link Optional#empty()}
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Verilen e-posta adresinin veritabanında kayıtlı olup olmadığını kontrol eder.
     *
     * @param email müşteri e-posta adresi
     * @return varsa {@code true}, yoksa {@code false}
     */
    boolean existsByEmail(String email);
}
