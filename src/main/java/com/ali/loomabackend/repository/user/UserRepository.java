package com.ali.loomabackend.repository.user;


import com.ali.loomabackend.model.entity.user.User;
import com.ali.loomabackend.model.enums.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);


    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deleted = false")
    Page<User> findActiveUsers(@Param("status") UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllNonDeleted(Pageable pageable);

    List<User> findAllByIdIn(List<UUID> ids);
}