package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    List<Invoice> findByBuyerIdOrderByIssuedAtDesc(Long buyerId);
    boolean existsByPaymentId(Long paymentId);
    Optional<Invoice> findByPaymentId(Long paymentId);
}
