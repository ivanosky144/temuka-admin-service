package com.temuka.insight_service.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "search_indexes")
public class SearchIndex {

    @Id
    private String id; // Could be postId, majorId, universityId, etc.

    @TextIndexed(weight=5)
    private String title;

    @TextIndexed(weight=1)
    private String content;

    private String type; // "post", "major", "university", "community", "review", "comment" etc.

    private String contextId;

    private Instant createdAt;

    private double scoreMultiplier;

    private String icon;

    private String slug;

    @TextScore
    private Float textScore;
}
