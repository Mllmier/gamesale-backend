package com.backend.gamesales.Services;

import com.backend.gamesales.Repository.UsersRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Data {
    @Autowired

    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


}
