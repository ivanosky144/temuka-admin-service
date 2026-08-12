package com.temuka.insight_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.temuka.insight_service.dto.response.SearchHistoryResponseDTO;
import com.temuka.insight_service.dto.response.SearchSuggestResponseDTO;
import com.temuka.insight_service.entity.SearchHistory;
import com.temuka.insight_service.service.SearchHistoryService;
import com.temuka.insight_service.service.SuggestionIndexService;
import com.temuka.insight_service.util.RestResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SuggestionIndexService suggestionIndexService;
    private final SearchHistoryService searchHistoryService;

    @GetMapping("/suggestions")
    public ResponseEntity<RestResponse<SearchSuggestResponseDTO>> handleTypeahead(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String contextId) {

        SearchSuggestResponseDTO suggestions = suggestionIndexService.getSuggestions(q, contextId);

        RestResponse<SearchSuggestResponseDTO> response = RestResponse.<SearchSuggestResponseDTO>builder()
                .message("Search suggestions retrieved successfully")
                .data(suggestions)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/click")
    public ResponseEntity<RestResponse<Void>> recordSearchClick(
            @RequestParam String userId,
            @RequestParam String query,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) SearchHistory.EntityType entityType,
            @RequestParam(required = false) String slug) {

        searchHistoryService.saveSearchClick(userId, query, entityId, entityType, slug);

        RestResponse<Void> response = RestResponse.<Void>builder()
                .message("Search click recorded successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<RestResponse<SearchHistoryResponseDTO>> getSearchHistory(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        SearchHistoryResponseDTO historyData = searchHistoryService.getUserHistory(userId, limit);

        RestResponse<SearchHistoryResponseDTO> response = RestResponse.<SearchHistoryResponseDTO>builder()
                .message("Search history retrieved successfully")
                .data(historyData)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/history")
    public ResponseEntity<RestResponse<Void>> clearSearchHistory(
            @RequestParam String userId) {

        searchHistoryService.clearHistory(userId);

        RestResponse<Void> response = RestResponse.<Void>builder()
                .message("Search history cleared successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}