package com.temuka.insight_service.consumer;

import java.time.Instant;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.temuka.insight_service.dto.event.SuggestionEventDTO;
import com.temuka.insight_service.entity.SuggestionIndex;
import com.temuka.insight_service.entity.SuggestionIndex.EntityType;
import com.temuka.insight_service.repository.SuggestionIndexRepository;
import com.temuka.insight_service.service.SuggestionIndexService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuggestionConsumer {

    private final SuggestionIndexRepository repository;
    private final SuggestionIndexService suggestionIndexService;

    @RabbitListener(queuesToDeclare = @org.springframework.amqp.rabbit.annotation.Queue(
        name = "${rabbitmq.suggestion.sync.queue.name:suggestion.sync}", 
        durable = "true"
    ))
    public void handleSuggestionEvent(SuggestionEventDTO event) {
        if (event == null || event.getEntityId() == null || event.getOperation() == null) {
            log.warn("Received invalid or null event payload: {}", event);
            return;
        }

        try {
            String op = event.getOperation().toUpperCase();

            switch (op) {
                case "DELETE" -> handleDelete(event.getEntityId(), parseEntityType(event.getType()));
                case "CREATE", "UPDATE" -> handleUpsert(event);
                case "VIEW", "LIKE", "JOIN", "SEARCH_CLICK" -> handleInteraction(event);
                default -> log.warn("Unknown event operation: {}", op);
            }
        } catch (Exception e) {
            log.error("Failed to process event for entityId: {}", event.getEntityId(), e);
            throw e; 
        }
    }

    private void handleDelete(String entityId, EntityType type) {
        if (type != null) {
            repository.deleteByEntityIdAndType(entityId, type);
        } else {
            repository.deleteByEntityId(entityId);
        }
    }

    private void handleUpsert(SuggestionEventDTO event) {
        Map<String, Object> data = event.getData();
        if (data == null) return;

        EntityType entityType = parseEntityType(event.getType());

        SuggestionIndex index = repository.findByEntityIdAndType(event.getEntityId(), entityType)
            .orElseGet(() -> {
                double baseWeight = suggestionIndexService.getInitialBaseWeight(entityType);
                return SuggestionIndex.builder()
                    .entityId(event.getEntityId())
                    .type(entityType)
                    .baseWeight(baseWeight)
                    .createdAt(Instant.now())
                    .build();
            });

        if (data.get("title") != null) index.setTitle(data.get("title").toString());
        if (data.get("content") != null) index.setContent(data.get("content").toString());
        if (data.get("context_id") != null) index.setContextId(data.get("context_id").toString());

        if (data.get("icon") != null) index.getMetadata().put("icon", data.get("icon").toString());
        if (data.get("slug") != null) index.getMetadata().put("slug", data.get("slug").toString());

        index.setUpdatedAt(Instant.now());
        index.setScoreMultiplier(suggestionIndexService.computeDecayedScore(index));

        repository.save(index);
    }

    private void handleInteraction(SuggestionEventDTO event) {
        EntityType entityType = parseEntityType(event.getType());

        repository.findByEntityIdAndType(event.getEntityId(), entityType).ifPresent(index -> {
            long durationSeconds = extractDuration(event.getData());
            String op = event.getOperation().toUpperCase();

            switch (op) {
                case "VIEW" -> {
                    index.setViewCount(safeGet(index.getViewCount()) + 1);
                    index.setTotalDwellTimeSeconds(safeGet(index.getTotalDwellTimeSeconds()) + durationSeconds);
                }
                case "LIKE" -> index.setLikeCount(safeGet(index.getLikeCount()) + 1);
                case "JOIN" -> index.setJoinCount(safeGet(index.getJoinCount()) + 1);
                case "SEARCH_CLICK" -> index.setSearchClickCount(safeGet(index.getSearchClickCount()) + 1);
            }

            double delta = suggestionIndexService.calculateOperationScoreDelta(op, durationSeconds);
            index.setPopularityScore(safeGetDouble(index.getPopularityScore()) + delta);
            index.setScoreMultiplier(suggestionIndexService.computeDecayedScore(index));
            index.setUpdatedAt(Instant.now());

            repository.save(index);
        });
    }

    private EntityType parseEntityType(Object typeObj) {
        if (typeObj == null) return null;
        if (typeObj instanceof EntityType et) return et;
        try {
            return EntityType.valueOf(typeObj.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid EntityType value: {}", typeObj);
            return null;
        }
    }

    private long extractDuration(Map<String, Object> data) {
        if (data == null || !data.containsKey("duration_seconds")) return 0L;
        Object val = data.get("duration_seconds");
        if (val instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private long safeGet(Long val) {
        return val != null ? val : 0L;
    }

    private double safeGetDouble(Double val) {
        return val != null ? val : 0.0;
    }
}