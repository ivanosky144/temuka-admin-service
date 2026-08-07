package com.temuka.insight_service.dto.event;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestionEventDTO {

    private String operation;

    private String type;

    @JsonProperty("entity_id")
    private String entityId;

    private Map<String, Object> data;
}
