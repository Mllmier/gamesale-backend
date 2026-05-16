package com.backend.gamesales.Model;

import jakarta.persistence.*;
import lombok.*;


@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name="tags")
public class  Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;


}
