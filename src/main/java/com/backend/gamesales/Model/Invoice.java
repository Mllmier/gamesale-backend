package com.backend.gamesales.Model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @Column(nullable =false,unique = true)
    private String invoiceNumber;

    @Column(nullable=false)
    private Long buyerId;
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name ="payment_id",nullable=false)
    private Payment payment;
    @Column(nullable = false)
    private String gameTitle;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String buyerEmail;

    private String buyerFullName;

    private String buyerCountry;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
    }

}
