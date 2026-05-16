package com.backend.gamesales.Model;

import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Enums.TypeSeller;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt= LocalDateTime.now();;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TypeSeller typeSeller;

    @Column
    private String companyName;

    @Column
    private String companyId;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private String rejectionReason;

    @Column(name = "stripe_account_id", unique = true)
    private String stripeAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable =false)
    private StatusSeller status;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-seller")
    private Users user;

    @Column(nullable = false)
    private Boolean active = true;

}