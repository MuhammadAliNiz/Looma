package com.ali.loomabackend.repository.user;


import com.ali.loomabackend.model.entity.user.TempUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TempUserRepository extends JpaRepository<TempUser, UUID> {

    Optional<TempUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM TempUser tu WHERE tu.expiryDate < :date OR tu.isUsed = true")
    void deleteExpiredOrUsed(@Param("date") LocalDateTime date);

    void deleteByEmail(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 100) String email);
}
