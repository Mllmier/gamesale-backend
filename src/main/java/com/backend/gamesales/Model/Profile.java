package com.backend.gamesales.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=true)
    private String firstName;

    @Column(nullable=true)
    private String lastName;

    @Column(nullable=true)
    private String bio;

    @Column(nullable=true)
    private String avatarUrl;

    @Column(nullable=true)
    private String country;

    @Column(nullable=true)
    private String phoneNumber;

    @OneToOne
    @JoinColumn(name="user_id")
    private Users user;

}
