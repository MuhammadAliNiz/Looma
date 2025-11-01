package com.ali.loomabackend.service.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ali.loomabackend.model.document.UserSearchDocument;
import com.ali.loomabackend.model.dto.response.PagedResponse;
import com.ali.loomabackend.model.dto.response.user.UserSearchResponse;
import com.ali.loomabackend.model.entity.user.User;
import com.ali.loomabackend.model.entity.user.UserProfile;
import com.ali.loomabackend.model.entity.user.UserStats;
import com.ali.loomabackend.repository.search.UserSearchRepository;
import com.ali.loomabackend.repository.user.UserProfileRepository;
import com.ali.loomabackend.repository.user.UserRepository;
import com.ali.loomabackend.repository.user.UserStatsRepository;
import com.ali.loomabackend.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {

    private final UserSearchRepository userSearchRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserStatsRepository userStatsRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final S3Service s3Service;

    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public PagedResponse<UserSearchResponse> searchUsers(String query, int page, int size) {
        if (query == null || query.trim().isEmpty()) {
            log.debug("Empty search query received");
            return createEmptyPagedResponse(page, size);
        }

        page = Math.max(0, page);
        size = (size <= 0 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;

        try {
            String searchTerm = query.trim().toLowerCase();
            log.info("Searching users with query: '{}', page: {}, size: {}", searchTerm, page, size);

            // Build Elasticsearch query
            Query searchQuery = buildElasticsearchQuery(searchTerm);

            // Create pageable with sorting by followers count
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "followersCount"));

            // Build native query
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(searchQuery)
                    .withPageable(pageable)
                    .build();

            // Execute search
            SearchHits<UserSearchDocument> searchHits = elasticsearchOperations.search(
                    nativeQuery,
                    UserSearchDocument.class
            );

            // Convert to response DTOs
            List<UserSearchResponse> results = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // Get total count
            long totalElements = searchHits.getTotalHits();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            log.info("Found {} users matching query: '{}' (total: {})", results.size(), searchTerm, totalElements);

            return PagedResponse.<UserSearchResponse>builder()
                    .content(results)
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .isFirst(page == 0)
                    .isLast(page >= totalPages - 1)
                    .build();

        } catch (Exception e) {
            log.error("Error searching users with query: '{}', page: {}, size: {}", query, page, size, e);
            return createEmptyPagedResponse(page, size);
        }
    }


    private Query buildElasticsearchQuery(String searchTerm) {

        return Query.of(q -> q.bool(BoolQuery.of(b -> b
                // Username matches (highest priority)
                .should(s -> s.match(m -> m
                        .field("username")
                        .query(searchTerm)
                        .boost(3.0f)
                ))
                // First name matches
                .should(s -> s.match(m -> m
                        .field("firstName")
                        .query(searchTerm)
                        .boost(2.0f)
                ))
                // Last name matches
                .should(s -> s.match(m -> m
                        .field("lastName")
                        .query(searchTerm)
                        .boost(2.0f)
                ))
                // Display name matches
                .should(s -> s.match(m -> m
                        .field("displayName")
                        .query(searchTerm)
                        .boost(2.0f)
                ))
                // Prefix match for autocomplete (username)
                .should(s -> s.matchPhrasePrefix(m -> m
                        .field("username")
                        .query(searchTerm)
                        .boost(2.5f)
                ))
                // Prefix match for autocomplete (firstName)
                .should(s -> s.matchPhrasePrefix(m -> m
                        .field("firstName")
                        .query(searchTerm)
                        .boost(1.5f)
                ))
                // Prefix match for autocomplete (lastName)
                .should(s -> s.matchPhrasePrefix(m -> m
                        .field("lastName")
                        .query(searchTerm)
                        .boost(1.5f)
                ))
                // Must be active user
                .must(m -> m.term(t -> t
                        .field("isActive")
                        .value(true)
                ))
                // At least one should clause must match
                .minimumShouldMatch("1")
        )));
    }


    /*
    public PagedResponse<UserSearchResponse> searchUsersWithFilters(
            String query,
            Boolean emailVerified,
            Long minFollowers,
            int page,
            int size) {

        if (query == null || query.trim().isEmpty()) {
            return createEmptyPagedResponse(page, size);
        }

        page = Math.max(0, page);
        size = (size <= 0 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;

        try {
            String searchTerm = query.trim().toLowerCase();
            log.info("Filtered search: query='{}', verified={}, minFollowers={}, page={}, size={}",
                    searchTerm, emailVerified, minFollowers, page, size);

            // Build query with filters
            Query searchQuery = buildFilteredQuery(searchTerm, emailVerified, minFollowers);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "followersCount"));

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(searchQuery)
                    .withPageable(pageable)
                    .build();

            SearchHits<UserSearchDocument> searchHits = elasticsearchOperations.search(
                    nativeQuery,
                    UserSearchDocument.class
            );

            List<UserSearchResponse> results = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            long totalElements = searchHits.getTotalHits();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            return PagedResponse.<UserSearchResponse>builder()
                    .content(results)
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .isFirst(page == 0)
                    .isLast(page >= totalPages - 1)
                    .build();

        } catch (Exception e) {
            log.error("Error in filtered search: query='{}', verified={}, minFollowers={}",
                    query, emailVerified, minFollowers, e);
            return createEmptyPagedResponse(page, size);
        }
    }


    private Query buildFilteredQuery(String searchTerm, Boolean emailVerified, Long minFollowers) {
        return Query.of(q -> q.bool(BoolQuery.of(b -> {
            // Add base search conditions
            b.should(s -> s.match(m -> m.field("username").query(searchTerm).boost(3.0f)))
                    .should(s -> s.match(m -> m.field("firstName").query(searchTerm).boost(2.0f)))
                    .should(s -> s.match(m -> m.field("lastName").query(searchTerm).boost(2.0f)))
                    .should(s -> s.match(m -> m.field("displayName").query(searchTerm).boost(2.0f)))
                    .should(s -> s.matchPhrasePrefix(m -> m.field("username").query(searchTerm).boost(2.5f)))
                    .must(m -> m.term(t -> t.field("isActive").value(true)))
                    .minimumShouldMatch("1");

            // Add verified filter if specified
            if (emailVerified != null) {
                b.must(m -> m.term(t -> t.field("emailVerified").value(emailVerified)));
            }

            // Add minimum followers filter if specified
            if (minFollowers != null && minFollowers > 0) {
                b.must(m -> m.range(r -> r
                        .field("followersCount")
                        .gte(co.elastic.clients.json.JsonData.of(minFollowers))
                ));
            }

            return b;
        })));
    }
     */

























    @Transactional
    public void indexUser(UUID userId) {
        if (userId == null) {
            log.warn("Cannot index user: userId is null");
            return;
        }

        try {
            log.debug("Indexing user: {}", userId);

            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                log.warn("User not found for indexing: {}", userId);
                return;
            }

            User user = userOptional.get();

            // Skip inactive users
            if (!user.isActive()) {
                log.info("Skipping indexing for inactive user: {}", userId);
                deleteUserFromIndex(userId);
                return;
            }

            // Fetch related data
            UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
            UserStats stats = userStatsRepository.findByUserId(userId).orElse(null);

            // Build search document
            UserSearchDocument document = UserSearchDocument.builder()
                    .id(userId.toString())
                    .userId(userId)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(profile != null ? profile.getFirstName() : null)
                    .lastName(profile != null ? profile.getLastName() : null)
                    .displayName(profile != null ? profile.getDisplayName() : null)
                    .bio(profile != null ? profile.getBio() : null)
                    .profilePictureUrl(profile != null ? profile.getProfilePictureUrl() : null)
                    .isActive(user.isActive())
                    .emailVerified(user.getEmailVerified())
                    .followersCount(stats != null ? stats.getFollowersCount() : 0L)
                    .followingCount(stats != null ? stats.getFollowingCount() : 0L)
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();

            userSearchRepository.save(document);
            log.info("Successfully indexed user: {} (username: {})", userId, user.getUsername());

        } catch (Exception e) {
            log.error("Error indexing user: {}", userId, e);
        }
    }

    @Transactional
    public int indexUsers(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("Cannot index users: userIds list is null or empty");
            return 0;
        }

        log.info("Starting bulk indexing for {} users", userIds.size());
        int successCount = 0;

        for (UUID userId : userIds) {
            try {
                indexUser(userId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to index user: {}", userId, e);
            }
        }

        log.info("Bulk indexing completed. Success: {}/{}", successCount, userIds.size());
        return successCount;
    }

    @Transactional
    public void deleteUserFromIndex(UUID userId) {
        if (userId == null) {
            log.warn("Cannot delete user from index: userId is null");
            return;
        }

        try {
            log.debug("Deleting user from index: {}", userId);
            userSearchRepository.deleteByUserId(userId);
            log.info("Successfully deleted user from index: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user from index: {}", userId, e);
        }
    }

    @Transactional(readOnly = true)
    public IndexingStats indexAllUsers() {
        log.info("Starting full user indexing...");

        int totalCount = 0;
        int successCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        try {
            List<User> users = userRepository.findAll();
            totalCount = users.size();
            log.info("Found {} total users to index", totalCount);

            for (User user : users) {
                try {
                    if (user.isActive()) {
                        indexUser(user.getId());
                        successCount++;
                    } else {
                        skippedCount++;
                        log.debug("Skipped inactive user: {}", user.getId());
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.error("Error indexing user: {}", user.getId(), e);
                }
            }

            log.info("Full indexing completed. Total: {}, Success: {}, Skipped: {}, Failed: {}",
                    totalCount, successCount, skippedCount, failedCount);

            return new IndexingStats(totalCount, successCount, skippedCount, failedCount);

        } catch (Exception e) {
            log.error("Fatal error during full user indexing", e);
            throw new RuntimeException("Failed to complete full user indexing", e);
        }
    }

    @Transactional
    public void reindexUser(UUID userId) {
        if (userId == null) {
            log.warn("Cannot reindex user: userId is null");
            return;
        }

        log.info("Re-indexing user: {}", userId);
        deleteUserFromIndex(userId);
        indexUser(userId);
    }

    @Transactional
    public void updateUserStats(UUID userId) {
        if (userId == null) {
            log.warn("Cannot update user stats: userId is null");
            return;
        }

        try {
            log.debug("Updating user stats: {}", userId);

            List<UserSearchDocument> documents = userSearchRepository.findByUserId(userId);
            if (documents.isEmpty()) {
                log.warn("User not found in index for stats update: {}", userId);
                indexUser(userId);
                return;
            }

            UserStats stats = userStatsRepository.findByUserId(userId).orElse(null);
            if (stats == null) {
                log.warn("User stats not found: {}", userId);
                return;
            }

            for (UserSearchDocument document : documents) {
                document.setFollowersCount(stats.getFollowersCount());
                document.setFollowingCount(stats.getFollowingCount());
                userSearchRepository.save(document);
            }

            log.info("Successfully updated user stats: {}", userId);

        } catch (Exception e) {
            log.error("Error updating user stats: {}", userId, e);
        }
    }

    public long getIndexedUserCount() {
        try {
            return userSearchRepository.count();
        } catch (Exception e) {
            log.error("Error getting indexed user count", e);
            return 0;
        }
    }

    public boolean isUserIndexed(UUID userId) {
        if (userId == null) {
            return false;
        }

        try {
            return !userSearchRepository.findByUserId(userId).isEmpty();
        } catch (Exception e) {
            log.error("Error checking if user is indexed: {}", userId, e);
            return false;
        }
    }


    private UserSearchResponse convertToResponse(UserSearchDocument document) {
        if (document == null) {
            return null;
        }

        String profilePicUrl = null;
        if (document.getProfilePictureUrl() != null && !document.getProfilePictureUrl().isEmpty()) {
            try {
                profilePicUrl = s3Service.getFileUrl(document.getProfilePictureUrl());
            } catch (Exception e) {
                log.warn("Error generating S3 URL for user: {}", document.getUserId(), e);
            }
        }

        return UserSearchResponse.builder()
                .userId(document.getUserId())
                .username(document.getUsername())
                .firstName(document.getFirstName())
                .lastName(document.getLastName())
                .displayName(document.getDisplayName())
                .bio(document.getBio())
                .profilePictureUrl(profilePicUrl)
                .followersCount(document.getFollowersCount())
                .followingCount(document.getFollowingCount())
                .emailVerified(document.getEmailVerified())
                .build();
    }

    private PagedResponse<UserSearchResponse> createEmptyPagedResponse(int page, int size) {
        return PagedResponse.<UserSearchResponse>builder()
                .content(new ArrayList<>())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class IndexingStats {
        private int totalCount;
        private int successCount;
        private int skippedCount;
        private int failedCount;
    }
}