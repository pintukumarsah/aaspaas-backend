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
                // CSRF - disabled for JWT based REST API
                .csrf(csrf -> csrf.disable())

                // Stateless session for JWT auth
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // AUTHORIZATION RULES
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC AUTH APIs
                        .requestMatchers("/api/auth/**").permitAll()

                        // CATEGORIES
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")

                        // PRODUCTS
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("SELLER", "ADMIN")

                        // PRODUCT IMAGES
                        .requestMatchers(HttpMethod.GET, "/api/products/*/images").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/images").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/*/images/**").hasAnyRole("SELLER", "ADMIN")

                        // CART
                        .requestMatchers("/api/cart/**").authenticated()

                        // ORDERS
                        .requestMatchers("/api/orders/**").authenticated()

                        // ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // DELIVERY REQUESTS / PARTNERS / QUOTES (must be declared before
                        // /api/delivery/** below, otherwise the broader delivery matcher
                        // will always match first)
                        .requestMatchers("/api/delivery/requests/**").authenticated()
                        .requestMatchers("/api/delivery/partners/**").authenticated()
                        .requestMatchers("/api/delivery/quotes/**").authenticated()
                        .requestMatchers("/api/delivery/assignments/**").authenticated()

                        // DELIVERY PARTNER
                        .requestMatchers("/api/delivery/**").hasAnyRole("DELIVERY_PARTNER", "ADMIN")

                        // SERVICE PROVIDER
                        .requestMatchers("/api/service-provider/**").hasAnyRole("SERVICE_PROVIDER", "ADMIN")

                        // BUSINESS
                        .requestMatchers("/api/businesses/**").hasAnyRole("SELLER", "ADMIN")

                        // USERS
                        .requestMatchers("/api/users/**").hasAnyRole(
                                "CUSTOMER", "SELLER", "DELIVERY_PARTNER", "SERVICE_PROVIDER", "ADMIN"
                        )

                        // EVERYTHING ELSE
                        .anyRequest().authenticated()
                )

                // AUTHENTICATION PROVIDER
                .authenticationProvider(authenticationProvider)

                // JWT FILTER
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}