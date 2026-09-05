package com.aaspaas.aaspaas_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        AuthenticationProvider authenticationProvider) throws Exception {

                http
                                // Disable CSRF because we are using JWT-based stateless authentication
                                .csrf(csrf -> csrf.disable())

                                // No server-side session
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                // =========================
                                                // PUBLIC AUTH APIs
                                                // =========================
                                                .requestMatchers("/api/auth/**")
                                                .permitAll()

                                                // =========================
                                                // CATEGORIES
                                                // =========================

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/categories/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/categories")
                                                .hasRole("ADMIN")

                                                // =========================
                                                // PRODUCTS
                                                // =========================
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/products/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/products")
                                                .hasAnyRole("SELLER", "ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/products/**")
                                                .hasAnyRole("SELLER", "ADMIN")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/products/**")
                                                .hasAnyRole("SELLER", "ADMIN")

                                                // =========================
                                                // ADMIN
                                                // =========================
                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMIN")

                                                // =========================
                                                // SELLER
                                                // =========================
                                                .requestMatchers("/api/seller/**")
                                                .hasAnyRole("SELLER", "ADMIN")

                                                // =========================
                                                // DELIVERY PARTNER
                                                // =========================
                                                .requestMatchers("/api/delivery/**")
                                                .hasAnyRole("DELIVERY_PARTNER", "ADMIN")

                                                // =========================
                                                // SERVICE PROVIDER
                                                // =========================
                                                .requestMatchers("/api/service-provider/**")
                                                .hasAnyRole("SERVICE_PROVIDER", "ADMIN")

                                                // =========================
                                                // BUSINESS
                                                // =========================
                                                .requestMatchers("/api/businesses/**")
                                                .hasAnyRole("SELLER", "ADMIN")

                                                // =========================
                                                // USERS
                                                // =========================
                                                .requestMatchers("/api/users/**")
                                                .hasAnyRole(
                                                                "CUSTOMER",
                                                                "SELLER",
                                                                "DELIVERY_PARTNER",
                                                                "SERVICE_PROVIDER",
                                                                "ADMIN")

                                                // =========================
                                                // EVERYTHING ELSE
                                                // =========================
                                                .anyRequest()
                                                .authenticated())

                                // Authentication provider
                                .authenticationProvider(authenticationProvider)

                                // JWT filter before username/password authentication filter
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}