// package com.aaspaas.aaspaas_backend.security;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;

// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;

// @Component
// @RequiredArgsConstructor
// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     private final JwtService jwtService;
//     private final CustomUserDetailsService customUserDetailsService;

//     @Override
//     protected void doFilterInternal(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain
//     ) throws ServletException, IOException {

//         String authorizationHeader =
//                 request.getHeader("Authorization");

//         String token = null;
//         String phone = null;

//         // =====================================================
//         // 1. Read Authorization Header
//         // =====================================================

//         if (authorizationHeader != null
//                 && authorizationHeader.startsWith("Bearer ")) {

//             token = authorizationHeader.substring(7);

//             try {

//                 phone = jwtService.extractPhone(token);

//             } catch (Exception ex) {

//                 System.out.println(
//                         "JWT extraction failed: "
//                                 + ex.getMessage()
//                 );
//             }
//         }

//         // =====================================================
//         // 2. Authenticate User
//         // =====================================================

//         if (phone != null
//                 && SecurityContextHolder
//                 .getContext()
//                 .getAuthentication() == null) {

//             try {

//                 // Validate token first
//                 if (jwtService.isTokenValid(token)) {

//                     // Load user from database
//                     UserDetails userDetails =
//                             customUserDetailsService
//                                     .loadUserByUsername(phone);

//                     // Create authentication
//                     UsernamePasswordAuthenticationToken authentication =
//                             new UsernamePasswordAuthenticationToken(
//                                     userDetails,
//                                     null,
//                                     userDetails.getAuthorities()
//                             );

//                     authentication.setDetails(
//                             new WebAuthenticationDetailsSource()
//                                     .buildDetails(request)
//                     );

//                     // Set authentication
//                     SecurityContextHolder
//                             .getContext()
//                             .setAuthentication(authentication);

//                     // Temporary debugging
//                     System.out.println(
//                             "Authenticated user: "
//                                     + phone
//                     );

//                     System.out.println(
//                             "Authorities: "
//                                     + userDetails.getAuthorities()
//                     );
//                 }

//             } catch (Exception ex) {

//                 System.out.println(
//                         "JWT authentication failed for "
//                                 + phone
//                                 + ": "
//                                 + ex.getMessage()
//                 );

//                 ex.printStackTrace();
//             }
//         }

//         // =====================================================
//         // 3. Continue Request
//         // =====================================================

//         filterChain.doFilter(request, response);
//     }
// }

package com.aaspaas.aaspaas_backend.security;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        // No Bearer token: continue normally.
        // Protected routes will be rejected by Spring Security.
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate signature, token structure and expiry.
            if (!jwtService.isTokenValid(token)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            String phone = jwtService.extractPhone(token);

            if (phone == null || phone.isBlank()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Do not overwrite an existing authenticated context.
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        customUserDetailsService.loadUserByUsername(phone);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (JwtException
                 | IllegalArgumentException
                 | UsernameNotFoundException ex) {

            // Never log the token or sensitive authentication details.
            SecurityContextHolder.clearContext();
        }

        // For protected endpoints, the security chain will reject a request
        // that still has no authenticated principal.
        filterChain.doFilter(request, response);
    }
}