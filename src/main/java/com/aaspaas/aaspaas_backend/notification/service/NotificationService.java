package com.aaspaas.aaspaas_backend.notification.service;

import com.aaspaas.aaspaas_backend.notification.dto.NotificationResponse;
import com.aaspaas.aaspaas_backend.notification.dto.RegisterDeviceRequest;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(
            Long userId,
            String title,
            String message,
            String type,
            String referenceType,
            Long referenceId
    );

    List<NotificationResponse> getMyNotifications(
            int page,
            int size
    );

    List<NotificationResponse> getMyUnreadNotifications(
            int page,
            int size
    );

    long getUnreadCount();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void registerDevice(
            RegisterDeviceRequest request
    );

    void deactivateDevice(Long deviceId);
}