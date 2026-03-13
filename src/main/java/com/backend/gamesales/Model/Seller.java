package com.backend.gamesales.Model;

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
    private long id;

    @Column(nullable = false)
    private LocalDateTime createdAt= LocalDateTime.now();;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable=false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TypeSeller typeSeller;

    @Column
    private String companyName;

    @Column
    private String companyId;


    @Enumerated(EnumType.STRING)
    @Column(nullable =false)
    private StatusSeller status;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;



}
