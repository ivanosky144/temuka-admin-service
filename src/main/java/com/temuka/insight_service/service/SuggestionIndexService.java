package com.temuka.insight_service.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.temuka.insight_service.dto.response.SearchSuggestResponseDTO;
import com.temuka.insight_service.dto.response.RecommendationResponseDTO;
import com.temuka.insight_service.dto.data.SuggestionItemDTO;
import com.temuka.insight_service.entity.SuggestionIndex;
import com.temuka.insight_service.entity.SuggestionIndex.EntityType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionIndexService {

    private static final double GRAVITY = 1.5;
    private final MongoTemplate mongoTemplate;

    public SearchSuggestResponseDTO getSuggestions(String q, String contextId) {
        if (q == null || q.trim().isEmpty()) {
            return getTrendingSuggestions(contextId);
        }

        String cleanQuery = q.trim();
        Pattern searchPattern = Pattern.compile(Pattern.quote(cleanQuery), Pattern.CASE_INSENSITIVE);

        List<SuggestionItemDTO> communities = fetchCategorySuggestions(EntityType.COMMUNITY, searchPattern, contextId, 5);
        List<SuggestionItemDTO> majors = fetchCategorySuggestions(EntityType.MAJOR, searchPattern, contextId, 3);
        List<SuggestionItemDTO> universities = fetchCategorySuggestions(EntityType.UNIVERSITY, searchPattern, contextId, 3);
        List<SuggestionItemDTO> users = fetchCategorySuggestions(EntityType.USER, searchPattern, contextId, 3);
        List<SuggestionItemDTO> posts = fetchCategorySuggestions(EntityType.POST, searchPattern, contextId, 5);

        return SearchSuggestResponseDTO.builder()
                .query(cleanQuery)
                .communities(communities)
                .majors(majors)
                .universities(universities)
                .users(users)
                .posts(posts)
                .build();
    }

    public SearchSuggestResponseDTO getTrendingSuggestions(String contextId) {
        return SearchSuggestResponseDTO.builder()
                .query("")
                .communities(fetchTopTrending(EntityType.COMMUNITY, contextId, 5))
                .majors(fetchTopTrending(EntityType.MAJOR, contextId, 3))
                .universities(fetchTopTrending(EntityType.UNIVERSITY, contextId, 3))
                .posts(fetchTopTrending(EntityType.POST, contextId, 5))
                .users(List.of())
                .build();
    }

    private List<SuggestionItemDTO> fetchCategorySuggestions(EntityType type, Pattern pattern, String contextId, int limit) {
        Query query = new Query();

        Criteria criteria = Criteria.where("type").is(type)
                .orOperator(
                        Criteria.where("title").regex(pattern),
                        Criteria.where("content").regex(pattern)
                );

        if (contextId != null && !contextId.isBlank()) {
            criteria.and("contextId").is(contextId);
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "scoreMultiplier"));
        query.with(PageRequest.of(0, limit));

        return mongoTemplate.find(query, SuggestionIndex.class)
                .stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    private List<SuggestionItemDTO> fetchTopTrending(EntityType type, String contextId, int limit) {
        Query query = new Query();
        Criteria criteria = Criteria.where("type").is(type);

        if (contextId != null && !contextId.isBlank()) {
            criteria.and("contextId").is(contextId);
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "scoreMultiplier"));
        query.with(PageRequest.of(0, limit));

        return mongoTemplate.find(query, SuggestionIndex.class)
                .stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    private SuggestionItemDTO mapToItem(SuggestionIndex index) {
        Map<String, Object> meta = index.getMetadata() != null ? index.getMetadata() : Map.of();

        String icon = (String) meta.getOrDefault("logo", 
                        meta.getOrDefault("image", 
                        meta.getOrDefault("avatar", null)));

        String slug = (String) meta.get("slug");

        return SuggestionItemDTO.builder()
                .id(index.getEntityId()) 
                .title(index.getTitle())
                .type(index.getType() != null ? index.getType().name().toLowerCase() : null)
                .contextId(index.getContextId())
                .scoreMultiplier(index.getScoreMultiplier())
                .icon(icon)
                .slug(slug)
                .metadata(meta) 
                .build();
    }

    public double getInitialBaseWeight(EntityType entityType) {
        if (entityType == null) return 1.0;
        return switch (entityType) {
            case UNIVERSITY -> 3.0;
            case MAJOR -> 2.5;
            case COMMUNITY -> 2.0;
            case USER -> 1.2;
            case POST -> 1.0;
        };
    }

    public double getInitialBaseWeight(String entityType) {
        if (entityType == null) return 1.0;
        try {
            return getInitialBaseWeight(EntityType.valueOf(entityType.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return 1.0;
        }
    }

    public double calculateOperationScoreDelta(String operation, long durationSeconds) {
        if (operation == null) return 0.0;

        return switch (operation.toUpperCase()) {
            case "VIEW" -> {
                double dwellWeight = durationSeconds > 0 ? Math.log1p(durationSeconds) : 0.5;
                yield 0.2 + (0.5 * dwellWeight);
            }
            case "SEARCH_CLICK" -> 1.5;
            case "LIKE" -> 2.5;
            case "JOIN" -> 4.0;
            default -> 0.0;
        };
    }

    public double computeDecayedScore(SuggestionIndex index) {
        Instant created = index.getCreatedAt() != null ? index.getCreatedAt() : Instant.now();
        double ageInHours = Duration.between(created, Instant.now()).toMinutes() / 60.0;

        double totalNumerator = index.getBaseWeight() + index.getPopularityScore();
        double timeDecayDenominator = Math.pow(ageInHours + 2.0, GRAVITY);

        return totalNumerator / timeDecayDenominator;
    }

    public RecommendationResponseDTO getRecommendations(EntityType type, String contextId, int limit) {
        List<SuggestionItemDTO> items = fetchTopTrending(type, contextId, limit);
        return RecommendationResponseDTO.builder()
            .category(type.name().toLowerCase())
            .count(items.size())
            .items(items)
            .build();
    }

    public RecommendationResponseDTO getUniversityRecommendations(String contextId, int limit) {
        return getRecommendations(EntityType.UNIVERSITY, contextId, limit);
    }

    public RecommendationResponseDTO getMajorRecommendations(String contextId, int limit) {
        return getRecommendations(EntityType.MAJOR, contextId, limit);
    }

    public RecommendationResponseDTO getPostRecommendations(String contextId, int limit) {
        return getRecommendations(EntityType.POST, contextId, limit);
    }
}