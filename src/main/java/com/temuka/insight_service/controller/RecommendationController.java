package com.temuka.insight_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.temuka.insight_service.dto.response.RecommendationResponseDTO;
import com.temuka.insight_service.service.SuggestionIndexService;
import com.temuka.insight_service.util.RestResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final SuggestionIndexService suggestionIndexService;

@GetMapping("/universities")
    public ResponseEntity<RestResponse<RecommendationResponseDTO>> getUniversityRecommendations(
            @RequestParam(required = false) String contextId,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        RecommendationResponseDTO data = suggestionIndexService.getUniversityRecommendations(contextId, limit);

        return ResponseEntity.ok(RestResponse.<RecommendationResponseDTO>builder()
                .message("University recommendations retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/majors")
    public ResponseEntity<RestResponse<RecommendationResponseDTO>> getMajorRecommendations(
            @RequestParam(required = false) String contextId,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        RecommendationResponseDTO data = suggestionIndexService.getMajorRecommendations(contextId, limit);

        return ResponseEntity.ok(RestResponse.<RecommendationResponseDTO>builder()
                .message("Major recommendations retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/posts")
    public ResponseEntity<RestResponse<RecommendationResponseDTO>> getPostRecommendations(
            @RequestParam(required = false) String contextId,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        RecommendationResponseDTO data = suggestionIndexService.getPostRecommendations(contextId, limit);

        return ResponseEntity.ok(RestResponse.<RecommendationResponseDTO>builder()
                .message("Post recommendations retrieved successfully")
                .data(data)
                .build());
    }
}
