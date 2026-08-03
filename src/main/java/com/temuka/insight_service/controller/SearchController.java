package com.temuka.insight_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.temuka.insight_service.util.RestResponse;
import com.temuka.insight_service.dto.response.TypeaheadSearchResponseDTO;
import com.temuka.insight_service.service.SearchIndexService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchIndexService searchIndexService;

    @GetMapping("/suggest")
    public ResponseEntity<RestResponse<TypeaheadSearchResponseDTO>> handleTypeahead(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String contextId) {

        TypeaheadSearchResponseDTO suggestions = searchIndexService.getSuggestions(q, contextId);

        RestResponse<TypeaheadSearchResponseDTO> response = RestResponse.<TypeaheadSearchResponseDTO>builder()
                .message("Search suggestions retrieved successfully")
                .data(suggestions)
                .build();

        return ResponseEntity.ok(response);
    }
}