// package com.aaspaas.aaspaas_backend.security;

// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration
// @EnableWebSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//         private final JwtAuthenticationFilter jwtAuthenticationFilter;

//         @Bean
//         public SecurityFilterChain securityFilterChain(
//                         HttpSecurity http,
//                         AuthenticationProvider authenticationProvider) throws Exception {

//                 http
//                                 // CSRF - disabled for JWT based REST API
//                                 .csrf(csrf -> csrf.disable())

//                                 // Stateless session for JWT auth
//                                 .sessionManagement(session -> session
//                                                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

//                                 // AUTHORIZATION RULES
//                                 .authorizeHttpRequests(auth -> auth

//                                                 // PUBLIC AUTH APIs
//                                                 .requestMatchers("/api/auth/**").permitAll()

//                                                 // CATEGORIES
//                                                 .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
//                                                 .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")

//                                                 // PRODUCTS
//                                                 .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
//                                                 .requestMatchers(HttpMethod.POST, "/api/products")
//                                                 .hasAnyRole("SELLER", "ADMIN")
//                                                 .requestMatchers(HttpMethod.PUT, "/api/products/**")
//                                                 .hasAnyRole("SELLER", "ADMIN")
//                                                 .requestMatchers(HttpMethod.DELETE, "/api/products/**")
//                                                 .hasAnyRole("SELLER", "ADMIN")

//                                                 // PRODUCT IMAGES
//                                                 .requestMatchers(HttpMethod.GET, "/api/products/*/images").permitAll()
//                                                 .requestMatchers(HttpMethod.POST, "/api/products/*/images")
//                                                 .hasAnyRole("SELLER", "ADMIN")
//                                                 .requestMatchers(HttpMethod.DELETE, "/api/products/*/images/**")
//                                                 .hasAnyRole("SELLER", "ADMIN")

//                                                 // PAYMENT WEBHOOK - Razorpay calls this without JWT.
//                                                 // Signature verification is performed inside the webhook service.
//                                                 .requestMatchers("/api/payments/webhook/razorpay").permitAll()

//                                                 // PAYMENTS
//                                                 .requestMatchers("/api/payments/**").authenticated()

//                                                 // CART
//                                                 .requestMatchers("/api/cart/**").authenticated()

//                                                 // ORDERS
//                                                 .requestMatchers("/api/orders/**").authenticated()

//                                                 // ADMIN
//                                                 .requestMatchers("/api/admin/**").hasRole("ADMIN")

//                                                 // DELIVERY REQUESTS / PARTNERS / QUOTES (must be declared before
//                                                 // /api/delivery/** below, otherwise the broader delivery matcher
//                                                 // will always match first)
//                                                 .requestMatchers("/api/delivery/requests/**").authenticated()
//                                                 .requestMatchers("/api/delivery/partners/**").authenticated()
//                                                 .requestMatchers("/api/delivery/quotes/**").authenticated()
//                                                 .requestMatchers("/api/delivery/assignments/**").authenticated()
//                                                 .requestMatchers("/api/notifications/**").authenticated()
//                                                 .requestMatchers("/api/delivery/matching/**").authenticated()
//                                                 .requestMatchers("/api/delivery/routes/**").authenticated()
//                                                 .requestMatchers("/api/delivery/quotes/**").authenticated()
//                                                 .requestMatchers(
//         "/swagger-ui.html",
//         "/swagger-ui/**",
//         "/v3/api-docs",
//         "/v3/api-docs/**"
// ).permitAll()
//                                                 .requestMatchers("/api/admin/**")
//                                                 .hasRole("ADMIN")
//                                                 // DELIVERY PARTNER
//                                                 .requestMatchers("/api/delivery/**")
//                                                 .hasAnyRole("DELIVERY_PARTNER", "ADMIN")

//                                                 // SERVICE PROVIDER
//                                                 .requestMatchers("/api/service-provider/**")
//                                                 .hasAnyRole("SERVICE_PROVIDER", "ADMIN")

//                                                 // BUSINESS
//                                                 .requestMatchers("/api/businesses/**").hasAnyRole("SELLER", "ADMIN")

//                                                 // USERS
//                                                 .requestMatchers("/api/users/**").hasAnyRole(
//                                                                 "CUSTOMER", "SELLER", "DELIVERY_PARTNER",
//                                                                 "SERVICE_PROVIDER", "ADMIN")

//                                                 // EVERYTHING ELSE
//                                                 .anyRequest().authenticated())

//                                 // AUTHENTICATION PROVIDER
//                                 .authenticationProvider(authenticationProvider)

//                                 // JWT FILTER
//                                 .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

//                 return http.build();
//         }
// }

// package com.aaspaas.aaspaas_backend.security;

// import lombok.RequiredArgsConstructor;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

// @Configuration
// @EnableWebSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final JwtAuthenticationFilter jwtAuthenticationFilter;

//     private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

//     private final RestAccessDeniedHandler restAccessDeniedHandler;

//     private final ApiRateLimitFilter apiRateLimitFilter;
    

//     @Bean
//     public SecurityFilterChain securityFilterChain(
//             HttpSecurity http,
//             AuthenticationProvider authenticationProvider
//     ) throws Exception {

//         http
//                 // CORS: use the CorsConfigurationSource bean.
//                 .cors(Customizer.withDefaults())

//                 // CSRF disabled for this stateless Bearer-token REST API.
//                 // Revisit this if authentication changes to cookies.
//                 .csrf(csrf -> csrf.disable())

//                 // API requests do not use an HTTP session.
//                 .sessionManagement(session ->
//                         session.sessionCreationPolicy(
//                                 SessionCreationPolicy.STATELESS
//                         )
//                 )

//                 // Consistent JSON responses for authentication failures.
//                 .exceptionHandling(exceptions -> exceptions
//                         .authenticationEntryPoint(
//                                 restAuthenticationEntryPoint
//                         )
//                         .accessDeniedHandler(
//                                 restAccessDeniedHandler
//                         )
//                 )

//                 // Browser security response headers.
//                 .headers(headers -> headers
//                         .contentTypeOptions(Customizer.withDefaults())
//                         .frameOptions(frame -> frame.deny())
//                         .referrerPolicy(referrer -> referrer.policy(
//                                 ReferrerPolicyHeaderWriter.ReferrerPolicy
//                                         .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
//                         ))
//                 )

//                 // AUTHORIZATION RULES
//                 .authorizeHttpRequests(auth -> auth

//                         // Browser preflight requests.
//                         .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

//                         // PUBLIC AUTH APIs
//                         .requestMatchers("/api/auth/**").permitAll()

//                         // CATEGORIES
//                         .requestMatchers(
//                                 HttpMethod.GET,
//                                 "/api/categories/**"
//                         ).permitAll()

//                         .requestMatchers(
//                                 HttpMethod.POST,
//                                 "/api/categories"
//                         ).hasRole("ADMIN")

//                         // PRODUCTS
//                         .requestMatchers(
//                                 HttpMethod.GET,
//                                 "/api/products/**"
//                         ).permitAll()

//                         .requestMatchers(
//                                 HttpMethod.POST,
//                                 "/api/products"
//                         ).hasAnyRole("SELLER", "ADMIN")

//                         .requestMatchers(
//                                 HttpMethod.PUT,
//                                 "/api/products/**"
//                         ).hasAnyRole("SELLER", "ADMIN")

//                         .requestMatchers(
//                                 HttpMethod.DELETE,
//                                 "/api/products/**"
//                         ).hasAnyRole("SELLER", "ADMIN")

//                         // PRODUCT IMAGES
//                         .requestMatchers(
//                                 HttpMethod.GET,
//                                 "/api/products/*/images"
//                         ).permitAll()

//                         .requestMatchers(
//                                 HttpMethod.POST,
//                                 "/api/products/*/images"
//                         ).hasAnyRole("SELLER", "ADMIN")

//                         .requestMatchers(
//                                 HttpMethod.DELETE,
//                                 "/api/products/*/images/**"
//                         ).hasAnyRole("SELLER", "ADMIN")

//                         // CART
//                         .requestMatchers("/api/cart/**").authenticated()

//                         // ORDERS
//                         .requestMatchers("/api/orders/**").authenticated()

//                         // ADMIN
//                         .requestMatchers("/api/admin/**").hasRole("ADMIN")

//                         // Specific delivery routes must precede /api/delivery/**.
//                         .requestMatchers(
//                                 "/api/delivery/requests/**"
//                         ).authenticated()

//                         .requestMatchers(
//                                 "/api/delivery/partners/**"
//                         ).authenticated()

//                         .requestMatchers(
//                                 "/api/delivery/quotes/**"
//                         ).authenticated()

//                         .requestMatchers(
//                                 "/api/delivery/assignments/**"
//                         ).authenticated()

//                         .requestMatchers(
//                                 "/api/notifications/**"
//                         ).authenticated()

//                         .requestMatchers(
//                                 "/api/delivery/matching/**"
//                         ).authenticated()

//                         .requestMatchers(
//                                 "/api/delivery/routes/**"
//                         ).authenticated()

//                         // DELIVERY PARTNER
//                         .requestMatchers(
//                                 "/api/delivery/**"
//                         ).hasAnyRole("DELIVERY_PARTNER", "ADMIN")

//                         // SERVICE PROVIDER
//                         .requestMatchers(
//                                 "/api/service-provider/**"
//                         ).hasAnyRole("SERVICE_PROVIDER", "ADMIN")

//                         // BUSINESS
//                         .requestMatchers(
//                                 "/api/businesses/**"
//                         ).hasAnyRole("SELLER", "ADMIN")

//                         // USERS
//                         .requestMatchers(
//                                 "/api/users/**"
//                         ).hasAnyRole(
//                                 "CUSTOMER",
//                                 "SELLER",
//                                 "DELIVERY_PARTNER",
//                                 "SERVICE_PROVIDER",
//                                 "ADMIN"
//                         )

//                         // Keep the existing fallback rule.
//                         .anyRequest().authenticated()
//                 )

//                 // AUTHENTICATION PROVIDER
//                 .authenticationProvider(authenticationProvider)

//                 // JWT FILTER
//                 // .addFilterBefore(
//                 //         jwtAuthenticationFilter,
//                 //         UsernamePasswordAuthenticationFilter.class
//                 // );

//                 .addFilterBefore(
//         apiRateLimitFilter,
//         UsernamePasswordAuthenticationFilter.class
// )
// .addFilterBefore(
//         jwtAuthenticationFilter,
//         UsernamePasswordAuthenticationFilter.class
// );

//         return http.build();
//     }
// }

package com.aaspaas.aaspaas_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Built from YOUR current SecurityConfig (CORS, JSON 401/403, headers and
 * ApiRateLimitFilter are all kept). Added rules are marked "NEW".
 * First matching rule wins, so specific rules come before broad ones.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final ApiRateLimitFilter apiRateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy
                                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
.requestMatchers(
                "/v3/api-docs/**",
                "/v3/api-docs",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-ui/index.html",
                "/swagger-resources/**",
                "/webjars/**"
            ).permitAll()
                        // NEW: provider webhooks carry no JWT; authenticity is the HMAC signature
                        // verified inside the services.
                        .requestMatchers("/api/payments/webhook/razorpay").permitAll()
                        .requestMatchers("/api/payouts/webhook/razorpayx").permitAll()

                        // categories / products (unchanged)
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/products/*/images").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/*/images").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/*/images/**").hasAnyRole("SELLER", "ADMIN")

                        // NEW: public browsing of approved service listings
                        .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()

                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()

                        // NEW: explicit (was only covered by the fallback rule)
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/service-bookings/**").authenticated()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // NEW: commission create/reverse was open to ANY logged-in user
                        .requestMatchers("/api/commissions/**").hasRole("ADMIN")

                        // NEW: anyone logged in may read ratings, only customers/admin may write
                        .requestMatchers(HttpMethod.GET, "/api/ratings/**").authenticated()
                        .requestMatchers("/api/ratings/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // NEW: partner wallet (bank accounts + withdrawals)
                        .requestMatchers("/api/partner-wallet/**").hasRole("DELIVERY_PARTNER")

                        // delivery (specific before the broad rule, unchanged)
                        .requestMatchers("/api/delivery/requests/**").authenticated()
                        .requestMatchers("/api/delivery/partners/**").authenticated()
                        .requestMatchers("/api/delivery/quotes/**").authenticated()
                        .requestMatchers("/api/delivery/assignments/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/delivery/matching/**").authenticated()
                        .requestMatchers("/api/delivery/routes/**").authenticated()
                        .requestMatchers("/api/delivery/**").hasAnyRole("DELIVERY_PARTNER", "ADMIN")

                        .requestMatchers("/api/service-provider/**").hasAnyRole("SERVICE_PROVIDER", "ADMIN")
                        .requestMatchers("/api/businesses/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole(
                                "CUSTOMER", "SELLER", "DELIVERY_PARTNER", "SERVICE_PROVIDER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(apiRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
