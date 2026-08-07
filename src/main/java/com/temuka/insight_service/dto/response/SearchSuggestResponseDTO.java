package com.temuka.insight_service.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.temuka.insight_service.dto.data.SuggestionItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestResponseDTO {

    @JsonProperty("Query")
    private String query;

    @JsonProperty("Communities")
    private List<SuggestionItem> communities;

    @JsonProperty("Majors")
    private List<SuggestionItem> majors;

    @JsonProperty("Universities")
    private List<SuggestionItem> universities;

    @JsonProperty("Users")
    private List<SuggestionItem> users;

    @JsonProperty("Posts")
    private List<SuggestionItem> posts;

}