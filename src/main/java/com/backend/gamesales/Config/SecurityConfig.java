package com.backend.gamesales.Config;


import com.backend.gamesales.Filters.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/games/**").permitAll()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/order/**").authenticated()
                        .requestMatchers("/api/seller/request").authenticated()
                        .requestMatchers("/api/seller/status").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/seller/{userId}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/seller/approve/**").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/seller/reject/**").hasAuthority("ADMINISTRATOR")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMINISTRATOR")
                        .requestMatchers("/api/seller/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/promotions/active").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/promotions/game/**").permitAll()
                        .requestMatchers("/api/promotions/create").hasAuthority("SELLER")
                        .requestMatchers("/api/promotions/proposal").hasAuthority("ADMINISTRATOR")
                        .requestMatchers("/api/promotions/proposal/*/accept").hasAuthority("SELLER")
                        .requestMatchers("/api/promotions/proposal/*/reject").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/games/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/games/**").hasAuthority("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/games/**").hasAuthority("SELLER")
                        .requestMatchers("/api/profile/avatars/defaults").permitAll()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/wishlist/**").authenticated()
                        .requestMatchers("/api/payments/webhook").permitAll()
                        .requestMatchers("/api/library/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
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
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Requested-With", "ngrok-skip-browser-warning","Stripe-Signature"));
        configuration.setExposedHeaders(java.util.List.of("Authorization"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
