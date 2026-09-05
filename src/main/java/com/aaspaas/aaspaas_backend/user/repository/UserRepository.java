package com.aaspaas.aaspaas_backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aaspaas.aaspaas_backend.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}