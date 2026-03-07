package com.backend.gamesales.Config;

import com.backend.gamesales.Model.Role;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Configuration
public class Admin {
    @Bean
    CommandLineRunner initAdmin(UsersRepository usersRepository,
                                PasswordEncoder passwordEncoder) {

        return args -> {

            boolean adminExists = usersRepository.existsByRole(Role.ADMINISTRATOR);

            if (!adminExists) {

                Users admin = new Users();
                admin.setName("Administrador");
                admin.setLastname("Principal");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMINISTRATOR);
                admin.setBirthday(new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime());
                usersRepository.save(admin);

                System.out.println("create administrator");
            }
        };
    }
}
