package com.temuka.insight_service.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.temuka.insight_service.dto.data.SuggestionItemDTO;

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
    private List<SuggestionItemDTO> communities;

    @JsonProperty("Majors")
    private List<SuggestionItemDTO> majors;

    @JsonProperty("Universities")
    private List<SuggestionItemDTO> universities;

    @JsonProperty("Users")
    private List<SuggestionItemDTO> users;

    @JsonProperty("Posts")
    private List<SuggestionItemDTO> posts;

}