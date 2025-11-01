package com.ali.loomabackend.event.listener;

import com.ali.loomabackend.event.UserEvents.*;
import com.ali.loomabackend.service.search.UserSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSearchEventListener {

    private final UserSearchService userSearchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Indexing newly created user: {}", event.getUserId());
        try {
            userSearchService.indexUser(event.getUserId());
        } catch (Exception e) {
            log.error("Failed to index created user: {}", event.getUserId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserUpdated(UserUpdatedEvent event) {
        log.info("Re-indexing updated user: {}", event.getUserId());
        try {
            userSearchService.reindexUser(event.getUserId());
        } catch (Exception e) {
            log.error("Failed to re-index updated user: {}", event.getUserId(), e);
        }
    }

    /**
     * Remove user from index when deleted
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Removing deleted user from index: {}", event.getUserId());
        try {
            userSearchService.deleteUserFromIndex(event.getUserId());
        } catch (Exception e) {
            log.error("Failed to remove deleted user from index: {}", event.getUserId(), e);
        }
    }

    /**
     * Re-index user when profile updated
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("Re-indexing user after profile update: {}", event.getUserId());
        try {
            userSearchService.indexUser(event.getUserId());
        } catch (Exception e) {
            log.error("Failed to index after profile update: {}", event.getUserId(), e);
        }
    }

    /**
     * Update only stats (lightweight)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserStatsUpdated(UserStatsUpdatedEvent event) {
        log.debug("Updating user stats in index: {}", event.getUserId());
        try {
            userSearchService.updateUserStats(event.getUserId());
        } catch (Exception e) {
            log.error("Failed to update user stats: {}", event.getUserId(), e);
        }
    }
}