package com.ali.loomabackend.model.entity.user;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "temp_users", indexes = {
        @Index(name = "idx_temp_user_email", columnList = "email"),
        @Index(name = "idx_temp_user_expiry", columnList = "expiry_date"),
})
public class TempUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Name cannot be null")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Invalid email format")
    @Column(nullable = false, length = 100)
    private String email;

    @NotNull
    @Column(nullable = false)
    private Integer verificationCode;

    @Builder.Default
    @Column(nullable = false)
    private Boolean verified = false;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @Column(name = "date_of_birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !isUsed && !isExpired() && verified;
    }
}