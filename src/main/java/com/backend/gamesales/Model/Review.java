package com.backend.gamesales.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable=false)
    private LocalDate createdAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "game_id",nullable=false)
    private Game game;

    @ManyToOne
    @JoinColumn(name="user_id",nullable=false)
    private Users user;

    @PrePersist
    protected void OnCreated() {
        this.createdAt = LocalDate.now();
    }
}
