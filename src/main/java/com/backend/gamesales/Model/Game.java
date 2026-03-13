package com.backend.gamesales.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@Setter
@Getter
@Table(name="game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String title;
    @Column(nullable=false)
    private Double price;
    @Column(nullable = false)
    private String genre;
    @Column(nullable=false)
    private LocalDate releasedDate;
    @Column(nullable=false)
    private String description;
    @Column(nullable=false)
    private String developer;
    @Column(nullable=false)
    private String imageUrl;
    @Column(nullable=false)
    private String publisher;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;



}
