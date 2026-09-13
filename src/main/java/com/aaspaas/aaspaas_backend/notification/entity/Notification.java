package com.aaspaas.aaspaas_backend.notification.entity;

import com.aaspaas.aaspaas_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(
            name = "idx_notifications_user_id",
            columnList = "user_id"
        ),
        @Index(
            name = "idx_notifications_user_read",
            columnList = "user_id,is_read"
        ),
        @Index(
            name = "idx_notifications_created_at",
            columnList = "created_at"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

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
        nullable = false,
        length = 200
    )
    private String title;

    @Column(
        nullable = false,
        columnDefinition = "TEXT"
    )
    private String message;

    @Column(
        nullable = false,
        length = 50
    )
    private String type;

    @Column(
        name = "reference_type",
        length = 50
    )
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(
        name = "is_read",
        nullable = false
    )
    private Boolean isRead;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (isRead == null) {
            isRead = false;
        }

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}