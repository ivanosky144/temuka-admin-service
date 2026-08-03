package com.temuka.insight_service.service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.temuka.insight_service.dto.response.TypeaheadSearchResponseDTO;
import com.temuka.insight_service.dto.response.TypeaheadSearchResponseDTO.SearchSuggestionItem;
import com.temuka.insight_service.entity.SearchIndex;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final MongoTemplate mongoTemplate;

    public TypeaheadSearchResponseDTO getSuggestions(String q, String contextId) {
        if (q == null || q.trim().isEmpty()) {
            return getTrendingSuggestions(contextId);
        }

        String cleanQuery = q.trim();
        Pattern searchPattern = Pattern.compile(Pattern.quote(cleanQuery), Pattern.CASE_INSENSITIVE);

        List<SearchSuggestionItem> communities = fetchCategorySuggestions("community", searchPattern, contextId, 5);
        List<SearchSuggestionItem> majors = fetchCategorySuggestions("major", searchPattern, contextId, 3);
        List<SearchSuggestionItem> universities = fetchCategorySuggestions("university", searchPattern, contextId, 3);
        List<SearchSuggestionItem> users = fetchCategorySuggestions("user", searchPattern, contextId, 3);
        List<SearchSuggestionItem> posts = fetchCategorySuggestions("post", searchPattern, contextId, 5);

        return TypeaheadSearchResponseDTO.builder()
                .query(cleanQuery)
                .communities(communities)
                .majors(majors)
                .universities(universities)
                .users(users)
                .posts(posts)
                .build();
    }

    public TypeaheadSearchResponseDTO getTrendingSuggestions(String contextId) {
        return TypeaheadSearchResponseDTO.builder()
                .query("")
                .communities(fetchTopTrending("community", contextId, 5))
                .majors(fetchTopTrending("major", contextId, 3))
                .universities(fetchTopTrending("university", contextId, 3))
                .posts(fetchTopTrending("post", contextId, 5))
                .users(List.of())
                .build();
    }

    private List<SearchSuggestionItem> fetchCategorySuggestions(String type, Pattern pattern, String contextId, int limit) {
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

        return mongoTemplate.find(query, SearchIndex.class)
                .stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    private List<SearchSuggestionItem> fetchTopTrending(String type, String contextId, int limit) {
        Query query = new Query();
        Criteria criteria = Criteria.where("type").is(type);

        if (contextId != null && !contextId.isBlank()) {
            criteria.and("contextId").is(contextId);
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "scoreMultiplier"));
        query.with(PageRequest.of(0, limit));

        return mongoTemplate.find(query, SearchIndex.class)
                .stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    private SearchSuggestionItem mapToItem(SearchIndex index) {
        return SearchSuggestionItem.builder()
                .id(index.getId())
                .title(index.getTitle())
                .type(index.getType())
                .icon(index.getIcon())
                .slug(index.getSlug())
                .contextId(index.getContextId())
                .scoreMultiplier(index.getScoreMultiplier())
                .build();
    }
}