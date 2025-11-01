package com.ali.loomabackend.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

public class UserEvents {

    @Getter
    @AllArgsConstructor
    public static class UserCreatedEvent {
        private final UUID userId;
    }

    @Getter
    @AllArgsConstructor
    public static class UserUpdatedEvent {
        private final UUID userId;
    }

    @Getter
    @AllArgsConstructor
    public static class UserDeletedEvent {
        private final UUID userId;
    }

    @Getter
    @AllArgsConstructor
    public static class UserProfileUpdatedEvent {
        private final UUID userId;
    }

    @Getter
    @AllArgsConstructor
    public static class UserStatsUpdatedEvent {
        private final UUID userId;
    }
}