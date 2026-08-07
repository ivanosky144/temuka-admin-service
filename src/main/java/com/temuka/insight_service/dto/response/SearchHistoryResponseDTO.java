package com.temuka.insight_service.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.temuka.insight_service.dto.data.SearchHistoryItemDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponseDTO {
    
    @JsonProperty("UserId")
    private String userId;

    @JsonProperty("History")
    private List<SearchHistoryItemDTO> history;
}
