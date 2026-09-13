package com.aaspaas.aaspaas_backend.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private String type;

    private String referenceType;

    private Long referenceId;

    private Boolean isRead;

    private OffsetDateTime createdAt;
}