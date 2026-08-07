package com.temuka.insight_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "user_search_idx", def = "{'userId': 1, 'searchedAt': -1}")
public class SearchHistory {
    
    @Id
    private String id;

    private String userId;

    private String query;

    private String entityId;

    private EntityType entityType;

    private String slug;

    private Instant searchedAt;

    public enum EntityType {
        UNIVERSITY,
        MAJOR,
        COMMUNITY,
        USER,
        POST
    }
}
