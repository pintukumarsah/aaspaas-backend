package com.aaspaas.aaspaas_backend.notification.repository;

import com.aaspaas.aaspaas_backend.notification.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository
        extends JpaRepository<UserDevice, Long> {

    List<UserDevice>
    findByUserIdAndIsActiveTrue(
            Long userId
    );

    Optional<UserDevice>
    findByUserIdAndDeviceToken(
            Long userId,
            String deviceToken
    );
}