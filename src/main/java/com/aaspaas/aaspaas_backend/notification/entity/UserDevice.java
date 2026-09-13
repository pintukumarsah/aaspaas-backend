package com.aaspaas.aaspaas_backend.notification.entity;

import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "user_devices",
    indexes = {
        @Index(
            name = "idx_user_devices_user_id",
            columnList = "user_id"
        ),
        @Index(
            name = "idx_user_devices_active",
            columnList = "user_id,is_active"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @Column(
        name = "device_token",
        nullable = false,
        columnDefinition = "TEXT"
    )
    private String deviceToken;

    @Column(
        nullable = false,
        length = 20
    )
    private String platform;

    @Column(
        name = "is_active",
        nullable = false
    )
    private Boolean isActive;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}