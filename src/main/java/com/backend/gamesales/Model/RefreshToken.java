package com.backend.gamesales.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name="refresh_token" )
@AllArgsConstructor
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;

    @OneToOne
    @JoinColumn(name="user_Id",referencedColumnName = "id")
    private Users users;

    @Column(nullable =false,unique=true)
    private String token;

    @Column(nullable=false)
    private Instant expiryDate;

    public RefreshToken(){}

    public boolean isExpired(){
        return Instant.now().isAfter(this.expiryDate);
    }
}

