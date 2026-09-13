package com.aaspaas.aaspaas_backend.notification.controller;

import com.aaspaas.aaspaas_backend.notification.dto.NotificationResponse;
import com.aaspaas.aaspaas_backend.notification.dto.RegisterDeviceRequest;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getMyNotifications(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return ResponseEntity.ok(
                notificationService
                        .getMyNotifications(
                                page,
                                size
                        )
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>>
    getUnreadNotifications(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return ResponseEntity.ok(
                notificationService
                        .getMyUnreadNotifications(
                                page,
                                size
                        )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long>
    getUnreadCount() {

        return ResponseEntity.ok(
                notificationService
                        .getUnreadCount()
        );
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void>
    markAsRead(
            @PathVariable Long notificationId) {

        notificationService.markAsRead(
                notificationId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void>
    markAllAsRead() {

        notificationService.markAllAsRead();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/devices")
    public ResponseEntity<Void>
    registerDevice(
            @RequestBody RegisterDeviceRequest request) {

        notificationService.registerDevice(
                request
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<Void>
    deactivateDevice(
            @PathVariable Long deviceId) {

        notificationService.deactivateDevice(
                deviceId
        );

        return ResponseEntity.noContent().build();
    }
}