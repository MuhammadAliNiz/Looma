package com.ali.loomabackend.model.dto.request.post;

import com.ali.loomabackend.model.enums.post.PostVisibility;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    private PostVisibility visibility;
}