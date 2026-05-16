package com.backend.gamesales.Services.Jobs;

import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Model.Payment;
import com.backend.gamesales.Repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPendingPayment {
    private final PaymentRepository paymentRepository;


    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void execute() {
        log.info("[ProcessPendingPaymentsJob] Iniciando...");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        List<Payment> stalePendingPayments = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold);

        if (stalePendingPayments.isEmpty()) {
            log.info("[ProcessPendingPaymentsJob] No hay pagos pendientes expirados.");
            return;
        }

        for (Payment payment : stalePendingPayments) {
            payment.markAsCancelled();
            log.info("[ProcessPendingPaymentsJob] Pago cancelado | paymentId: {}", payment.getId());
        }

        paymentRepository.saveAll(stalePendingPayments);
        log.info("[ProcessPendingPaymentsJob] {} pagos cancelados.", stalePendingPayments.size());
    }
}
