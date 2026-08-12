package com.temuka.insight_service.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "suggestion_indexes")
@CompoundIndexes({
    @CompoundIndex(name = "type_score_idx", def = "{'type': 1, 'scoreMultiplier': -1}"),
    
    @CompoundIndex(name = "context_type_score_idx", def = "{'contextId': 1, 'type': 1, 'scoreMultiplier': -1}"),
    
    @CompoundIndex(name = "entity_type_id_unique_idx", def = "{'entityId': 1, 'type': 1}", unique = true)
})
public class SuggestionIndex {

    @Id
    private String id; 

    private String entityId; 

    private EntityType type; 

    @TextIndexed(weight = 5)
    private String title; 

    @TextIndexed(weight = 1)
    private String content; 

    @Indexed
    private String contextId; 

    private String icon;

    @Indexed(unique = true)
    private String slug;

    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Builder.Default
    @Field("baseWeight")
    private double baseWeight = 1.0;

    @Builder.Default
    @Field("popularityScore")
    private double popularityScore = 0.0;

    @Builder.Default
    @Field("scoreMultiplier")
    private double scoreMultiplier = 1.0;

    @Builder.Default
    private long viewCount = 0;

    @Builder.Default
    private long totalDwellTimeSeconds = 0;

    @Builder.Default
    private long likeCount = 0;

    @Builder.Default
    private long joinCount = 0;

    @Builder.Default
    private long searchClickCount = 0;

    public enum EntityType {
        UNIVERSITY,
        MAJOR,
        COMMUNITY,
        USER,
        POST
    }
}