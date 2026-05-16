package com.backend.gamesales.Repository;


import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Model.Payment;
import com.backend.gamesales.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    Optional<Payment> findByBuyerIdAndStatus(Long buyerId, PaymentStatus status);
    Optional<Payment> findByStripeSessionId(String stripeSessionId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold);
    List<Payment> findByBuyerOrderByCreatedAtDesc(Users buyer);



}
