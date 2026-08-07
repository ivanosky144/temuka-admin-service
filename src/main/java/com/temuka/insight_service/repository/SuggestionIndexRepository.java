package com.temuka.insight_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.temuka.insight_service.entity.SuggestionIndex;
import com.temuka.insight_service.entity.SuggestionIndex.EntityType;

@Repository
public interface SuggestionIndexRepository extends MongoRepository<SuggestionIndex, String> {

    Optional<SuggestionIndex> findByEntityIdAndType(String entityId, EntityType type);

    void deleteByEntityIdAndType(String entityId, EntityType type);

    void deleteByEntityId(String entityId);
}