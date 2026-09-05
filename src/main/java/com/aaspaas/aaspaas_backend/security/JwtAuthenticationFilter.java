package com.aaspaas.aaspaas_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

        String token = null;
        String phone = null;

        // =====================================================
        // 1. Read Authorization Header
        // =====================================================

        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            token = authorizationHeader.substring(7);

            try {

                phone = jwtService.extractPhone(token);

            } catch (Exception ex) {

                System.out.println(
                        "JWT extraction failed: "
                                + ex.getMessage()
                );
            }
        }

        // =====================================================
        // 2. Authenticate User
        // =====================================================

        if (phone != null
                && SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            try {

                // Validate token first
                if (jwtService.isTokenValid(token)) {

                    // Load user from database
                    UserDetails userDetails =
                            customUserDetailsService
                                    .loadUserByUsername(phone);

                    // Create authentication
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

                    // Set authentication
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    // Temporary debugging
                    System.out.println(
                            "Authenticated user: "
                                    + phone
                    );

                    System.out.println(
                            "Authorities: "
                                    + userDetails.getAuthorities()
                    );
                }

            } catch (Exception ex) {

                System.out.println(
                        "JWT authentication failed for "
                                + phone
                                + ": "
                                + ex.getMessage()
                );

                ex.printStackTrace();
            }
        }

        // =====================================================
        // 3. Continue Request
        // =====================================================

        filterChain.doFilter(request, response);
    }
}