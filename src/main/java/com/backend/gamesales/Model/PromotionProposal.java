package com.backend.gamesales.Model;

import com.backend.gamesales.Model.Enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double discountPercentage;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne
    private Game game;

    @ManyToOne
    private Users seller;

    @ManyToOne
    private Users admin;
}