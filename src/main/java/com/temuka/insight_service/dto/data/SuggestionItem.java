package com.temuka.insight_service.dto.data;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionItem {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Type")
    private String type; 

    @JsonProperty("ContextID")
    private String contextId;

    @JsonProperty("ScoreMultiplier")
    private double scoreMultiplier;

    @JsonProperty("Icon")
    private String icon;

    @JsonProperty("Slug")
    private String slug;

    @JsonProperty("Metadata")
    private Map<String, Object> metadata;
}
