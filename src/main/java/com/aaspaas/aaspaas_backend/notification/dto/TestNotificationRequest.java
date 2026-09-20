package com.aaspaas.aaspaas_backend.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestNotificationRequest {

    private Long userId;

    private String title;

    private String message;

    private String type;

    private String referenceType;

    private Long referenceId;
}