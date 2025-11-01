package com.ali.loomabackend.controller;

import com.ali.loomabackend.model.dto.response.ApiResponse;
import com.ali.loomabackend.model.dto.response.PagedResponse;
import com.ali.loomabackend.model.dto.response.user.UserSearchResponse;
import com.ali.loomabackend.service.search.UserSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "User Search", description = "Real-time user search endpoints with Elasticsearch")
public class UserSearchController {

    private final UserSearchService userSearchService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserSearchResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<UserSearchResponse> results = userSearchService.searchUsers(query, page, size);

        ApiResponse<PagedResponse<UserSearchResponse>> apiResponse = ApiResponse.success(
                results,
                String.format("Found %d users matching '%s'", results.getTotalElements(), query)
        );

        return ResponseEntity.ok(apiResponse);
    }





    /*
    @GetMapping("/users/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<UserSearchResponse>>> searchUsersWithFilters(
            @RequestParam String query,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Long minFollowers,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<UserSearchResponse> results = userSearchService.searchUsersWithFilters(
                query, verified, minFollowers, page, size
        );

        ApiResponse<PagedResponse<UserSearchResponse>> apiResponse = ApiResponse.success(
                results,
                "Search completed with filters"
        );

        return ResponseEntity.ok(apiResponse);
    }
     */

    @PostMapping("/index-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserSearchService.IndexingStats>> indexAllUsers() {
        UserSearchService.IndexingStats stats = userSearchService.indexAllUsers();

        ApiResponse<UserSearchService.IndexingStats> apiResponse = ApiResponse.success(
                stats,
                "User indexing completed"
        );

        return ResponseEntity.ok(apiResponse);
    }

}