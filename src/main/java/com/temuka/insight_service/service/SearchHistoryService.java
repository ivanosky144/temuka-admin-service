package com.temuka.insight_service.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.temuka.insight_service.dto.data.SearchHistoryItemDTO;
import com.temuka.insight_service.dto.response.SearchHistoryResponseDTO;
import com.temuka.insight_service.entity.SearchHistory;
import com.temuka.insight_service.repository.SearchHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository repository;

    public void saveSearchQuery(String userId, String query) {
        if (userId == null || query == null || query.trim().isEmpty()) {
            return;
        }

        String cleanQuery = query.trim();

        repository.deleteByUserIdAndQueryIgnoreCase(userId, cleanQuery);

        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .query(cleanQuery)
                .searchedAt(Instant.now())
                .build();

        repository.save(history);
    }

    public SearchHistoryResponseDTO getUserHistory(String userId, int limit) {
        if (userId == null) {
            return SearchHistoryResponseDTO.builder()
                    .userId(null)
                    .history(List.of())
                    .build();
        }

        List<SearchHistoryItemDTO> historyItems = repository.findByUserIdOrderBySearchedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(this::mapToItemDto)
                .collect(Collectors.toList());

        return SearchHistoryResponseDTO.builder()
                .userId(userId)
                .history(historyItems)
                .build();
    }

    public void clearHistory(String userId) {
        if (userId != null) {
            repository.deleteByUserId(userId);
        }
    }

    private SearchHistoryItemDTO mapToItemDto(SearchHistory entity) {
        String entityTypeStr = entity.getEntityType() != null 
                ? entity.getEntityType().name().toLowerCase() 
                : null;

        return SearchHistoryItemDTO.builder()
                .query(entity.getQuery())
                .entityId(entity.getEntityId())
                .entityType(entityTypeStr)
                .slug(entity.getSlug())
                .build();
    }
}