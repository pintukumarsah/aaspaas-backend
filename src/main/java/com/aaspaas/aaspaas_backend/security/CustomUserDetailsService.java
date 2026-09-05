package com.aaspaas.aaspaas_backend.security;

import com.aaspaas.aaspaas_backend.user.entity.Role;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone)
            throws UsernameNotFoundException {

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with phone: " + phone
                        )
                );

        Set<SimpleGrantedAuthority> authorities =
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .map(String::toUpperCase)
                        .map(roleName ->
                                new SimpleGrantedAuthority(
                                        "ROLE_" + roleName
                                )
                        )
                        .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhone())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!"ACTIVE".equalsIgnoreCase(user.getStatus()))
                .build();
    }
}