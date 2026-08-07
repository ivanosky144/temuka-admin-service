package com.temuka.insight_service.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.temuka.insight_service.entity.SearchHistory;

@Repository
public interface SearchHistoryRepository extends MongoRepository<SearchHistory, String> {

    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(String userId, Pageable pageable);

    void deleteByUserId(String userId);

    void deleteByUserIdAndQueryIgnoreCase(String userId, String query);
}