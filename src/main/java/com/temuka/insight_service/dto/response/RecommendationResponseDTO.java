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
public class RecommendationResponseDTO {
    
    @JsonProperty("Category")
    private String category;

    @JsonProperty("Count")
    private int count;

    @JsonProperty("Items")
    private List<SuggestionItemDTO> items;
}
