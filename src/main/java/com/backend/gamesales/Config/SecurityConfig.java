package com.backend.gamesales.Config;


import com.backend.gamesales.Filters.JwtAuthFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilters jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/seller/request").authenticated()
                        .requestMatchers("/api/seller/status").authenticated()
                        .requestMatchers("/api/seller/profile").authenticated()
                        .requestMatchers("/api/seller/approve/**").authenticated()
                        .requestMatchers("/api/seller/reject/**").authenticated()
                        .requestMatchers("/api/seller/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/games/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/games/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/games/**").authenticated()
                        .requestMatchers( "/api/profile/avatars/defaults").permitAll()
                        .requestMatchers( "/api/profile/seller/*").permitAll()
                        .requestMatchers("/api/profile/seller/**").hasAuthority("SELLER")
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers(
                                "/forgot-password/verify-mail/**",
                                "/forgot-password/verify-otp/**",
                                "/forgot-password/change-password"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}