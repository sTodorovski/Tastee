package com.example.tasteebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(securityContext -> securityContext.securityContextRepository(new HttpSessionSecurityContextRepository()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/error",
                                "/images/**",
                                "/stripe/webhook"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/restaurants/mine"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/restaurants"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/restaurants",
                                "/restaurants/{id}",
                                "/restaurants/{id}/dishes",
                                "/restaurants/{id}/reviews",
                                "/restaurants/{id}/reviews/summary",
                                "/api/dishes",
                                "/api/dishes/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}