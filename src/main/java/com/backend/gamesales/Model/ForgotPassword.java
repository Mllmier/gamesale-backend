package com.backend.gamesales.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPassword {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false)
    private Integer otp;

    @Column(nullable=false)
    private Date expirationTime;

    private boolean verified = false;


    @Column(nullable = false)
    private int resetRequestCount = 0;

    @Column
    private Date lastRequestDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false,unique = true)
    private Users users;


}
