package com.temuka.insight_service.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryItemDTO {
    
    @JsonProperty("Query")
    private String query;

    @JsonProperty("EntityID")
    private String entityId;

    @JsonProperty("EntityType")
    private String entityType;

    @JsonProperty("Slug")
    private String slug;
}