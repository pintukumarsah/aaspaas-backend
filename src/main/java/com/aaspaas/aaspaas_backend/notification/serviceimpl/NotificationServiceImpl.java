package com.aaspaas.aaspaas_backend.notification.serviceimpl;

import com.aaspaas.aaspaas_backend.notification.dto.NotificationResponse;
import com.aaspaas.aaspaas_backend.notification.dto.RegisterDeviceRequest;
import com.aaspaas.aaspaas_backend.notification.entity.Notification;
import com.aaspaas.aaspaas_backend.notification.entity.UserDevice;
import com.aaspaas.aaspaas_backend.notification.repository.NotificationRepository;
import com.aaspaas.aaspaas_backend.notification.repository.UserDeviceRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserDeviceRepository userDeviceRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(
            Long userId,
            String title,
            String message,
            String type,
            String referenceType,
            Long referenceId) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                ));

        Notification notification =
                Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .type(type)
                        .referenceType(referenceType)
                        .referenceId(referenceId)
                        .isRead(false)
                        .createdAt(
                                OffsetDateTime.now()
                        )
                        .build();

        notification =
                notificationRepository.save(
                        notification
                );

        /*
         * FCM will be called here later.
         *
         * Database notification is already
         * saved successfully.
         */

        return mapNotification(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getMyNotifications(
            int page,
            int size) {

        User user = getLoggedInUser();

        PageRequest pageRequest =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageRequest
                )
                .getContent()
                .stream()
                .map(this::mapNotification)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getMyUnreadNotifications(
            int page,
            int size) {

        User user = getLoggedInUser();

        PageRequest pageRequest =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                        user.getId(),
                        pageRequest
                )
                .getContent()
                .stream()
                .map(this::mapNotification)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {

        User user = getLoggedInUser();

        return notificationRepository
                .countByUserIdAndIsReadFalse(
                        user.getId()
                );
    }

    @Override
    @Transactional
    public void markAsRead(
            Long notificationId) {

        User user = getLoggedInUser();

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                ));

        if (!notification
                .getUser()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to update this notification"
            );
        }

        notification.setIsRead(true);

        notificationRepository.save(
                notification
        );
    }

    @Override
    @Transactional
    public void markAllAsRead() {

        User user = getLoggedInUser();

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                user.getId(),
                                PageRequest.of(
                                        0,
                                        1000
                                )
                        )
                        .getContent();

        notifications.forEach(
                notification ->
                        notification.setIsRead(true)
        );

        notificationRepository.saveAll(
                notifications
        );
    }

    @Override
    @Transactional
    public void registerDevice(
            RegisterDeviceRequest request) {

        User user = getLoggedInUser();

        if (request.getDeviceToken() == null
                || request.getDeviceToken().isBlank()) {

            throw new RuntimeException(
                    "Device token is required"
            );
        }

        String platform =
                request.getPlatform();

        if (platform == null
                || platform.isBlank()) {

            throw new RuntimeException(
                    "Platform is required"
            );
        }

        platform =
                platform.toUpperCase();

        if (!platform.equals("ANDROID")
                && !platform.equals("IOS")
                && !platform.equals("WEB")) {

            throw new RuntimeException(
                    "Platform must be ANDROID, IOS or WEB"
            );
        }

        UserDevice device =
                userDeviceRepository
                        .findByUserIdAndDeviceToken(
                                user.getId(),
                                request.getDeviceToken()
                        )
                        .orElse(null);

        if (device == null) {

            device =
                    UserDevice.builder()
                            .user(user)
                            .deviceToken(
                                    request.getDeviceToken()
                            )
                            .platform(platform)
                            .isActive(true)
                            .lastUsedAt(
                                    OffsetDateTime.now()
                            )
                            .createdAt(
                                    OffsetDateTime.now()
                            )
                            .build();

        } else {

            device.setPlatform(platform);
            device.setIsActive(true);
            device.setLastUsedAt(
                    OffsetDateTime.now()
            );
        }

        userDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public void deactivateDevice(
            Long deviceId) {

        User user = getLoggedInUser();

        UserDevice device =
                userDeviceRepository
                        .findById(deviceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Device not found"
                                ));

        if (!device
                .getUser()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to deactivate this device"
            );
        }

        device.setIsActive(false);

        userDeviceRepository.save(device);
    }

    private User getLoggedInUser() {

        String phone =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }

    private NotificationResponse
    mapNotification(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceType(
                        notification.getReferenceType()
                )
                .referenceId(
                        notification.getReferenceId()
                )
                .isRead(
                        notification.getIsRead()
                )
                .createdAt(
                        notification.getCreatedAt()
                )
                .build();
    }
} 
