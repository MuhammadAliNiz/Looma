package com.ali.loomabackend.model.entity.user;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "role_name")
})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "role_name", length = 50, unique = true, nullable = false)
    private String roleName;

    @Column(length = 255)
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // 🚫 NO bidirectional mapping to users
    // Fetch user-roles through UserRole join table entity
}