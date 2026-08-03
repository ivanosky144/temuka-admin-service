package com.temuka.insight_service.dto.response;

import java.util.List;
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
public class TypeaheadSearchResponseDTO {

    @JsonProperty("Query")
    private String query;

    @JsonProperty("Communities")
    private List<SearchSuggestionItem> communities;

    @JsonProperty("Majors")
    private List<SearchSuggestionItem> majors;

    @JsonProperty("Universities")
    private List<SearchSuggestionItem> universities;

    @JsonProperty("Users")
    private List<SearchSuggestionItem> users;

    @JsonProperty("Posts")
    private List<SearchSuggestionItem> posts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchSuggestionItem {

        @JsonProperty("ID")
        private String id;

        @JsonProperty("Title")
        private String title;

        @JsonProperty("Type")
        private String type; // "community", "major", "university", "user", "post"

        @JsonProperty("ContextID")
        private String contextId;

        @JsonProperty("ScoreMultiplier")
        private double scoreMultiplier;

        @JsonProperty("Icon")
        private String icon;

        @JsonProperty("Slug")
        private String slug;
    }
}