package com.aaspaas.aaspaas_backend.notification.serviceimpl;

import com.aaspaas.aaspaas_backend.notification.service.PushNotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebasePushNotificationServiceImpl
        implements PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendToDevice(
            String deviceToken,
            String title,
            String message,
            String referenceType,
            Long referenceId) {

        try {

            Message firebaseMessage =
                    Message.builder()
                            .setToken(deviceToken)
                            .setNotification(
                                    Notification.builder()
                                            .setTitle(title)
                                            .setBody(message)
                                            .build()
                            )
                            .putData(
                                    "referenceType",
                                    referenceType != null
                                            ? referenceType
                                            : ""
                            )
                            .putData(
                                    "referenceId",
                                    referenceId != null
                                            ? String.valueOf(
                                                    referenceId
                                            )
                                            : ""
                            )
                            .build();

            String response =
                    firebaseMessaging.send(
                            firebaseMessage
                    );

            log.info(
                    "FCM notification sent successfully. Response: {}",
                    response
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to send FCM notification to device",
                    ex
            );
        }
    }
}