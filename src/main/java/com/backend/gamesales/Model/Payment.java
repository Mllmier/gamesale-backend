package com.backend.gamesales.Model;
import com.backend.gamesales.Model.Enums.PaymentMethods;
import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Model.Enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Users buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(unique = true)
    private String stripeSessionId;

    @Column(length = 1000)
    private String stripeCheckoutUrl;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }

    public void markAsPaid() {
        this.status = PaymentStatus.SUCCEEDED;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void markAsDistributed() {
        this.status = PaymentStatus.DISTRIBUTED;}

}
