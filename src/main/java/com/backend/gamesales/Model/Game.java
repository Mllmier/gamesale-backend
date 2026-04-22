package com.backend.gamesales.Model;


import com.backend.gamesales.Model.Enums.CategoryGame;
import jakarta.persistence.*;
import lombok.*;
import org.aspectj.apache.bcel.generic.Tag;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Getter
@Builder
@Table(name="game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String title;
    @Column(nullable=false)
    private Double price;

    @Column(nullable=false)
    private LocalDate releaseDate;
    @Column(nullable=false)
    private String description;
    @Column(nullable=false)
    private String developer;
    @Column(nullable=false)
    private String imageUrl;
    @Column(nullable=false)
    private String publisher;
    @Column(nullable = false)
    private LocalDate createdAt;
    @Enumerated(EnumType.STRING)
    private CategoryGame categoryGame;
    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;


    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "game_tags",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tags> tags = new HashSet<>();
}
