package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Model.PaymentItem;
import com.backend.gamesales.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
    List<PaymentItem> findBySellerAndPayment_StatusOrderByPayment_PaidAtDesc(
            Users seller,
            PaymentStatus status
    );


}
