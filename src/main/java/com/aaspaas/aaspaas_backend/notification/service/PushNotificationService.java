package com.aaspaas.aaspaas_backend.notification.service;

public interface PushNotificationService {

    void sendToDevice(
            String deviceToken,
            String title,
            String message,
            String referenceType,
            Long referenceId
    );
}