package com.ali.loomabackend.repository.search;


import com.ali.loomabackend.model.document.UserSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, String> {

    Page<UserSearchDocument> findByUsernameContainingOrFirstNameContainingOrLastNameContainingOrDisplayNameContaining(
            String username, String firstName, String lastName, String displayName, Pageable pageable);

    List<UserSearchDocument> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}