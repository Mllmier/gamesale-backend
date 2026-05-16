package com.backend.gamesales.Model;

import com.backend.gamesales.Model.Enums.DiscountType;
import com.backend.gamesales.Model.Enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private Double discountPercentage;
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private PromotionStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;
    @ManyToOne
    private Users seller;
    @ManyToOne
    private Game game;

}
