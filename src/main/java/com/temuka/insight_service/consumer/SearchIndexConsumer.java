package com.temuka.insight_service.consumer;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.temuka.insight_service.dto.event.SearchSyncEventDTO;
import com.temuka.insight_service.entity.SearchIndex;
import com.temuka.insight_service.repository.SearchIndexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexConsumer {

    private final SearchIndexRepository searchIndexRepository;

    @RabbitListener(queues = "${rabbitmq.search.sync.queue.name:search.sync}")
    public void handleSearchSync(SearchSyncEventDTO event) {
        log.info("Received search sync event: {}", event);

        if (event == null || event.getEntityId() == null) {
            log.warn("Skipping search sync event due to missing payload or entityId: {}", event);
            return;
        }

        if ("DELETE".equalsIgnoreCase(event.getOperation())) {
            log.info("Deleting search index entry for entityId: {}", event.getEntityId());
            searchIndexRepository.deleteById(event.getEntityId());
            log.info("Successfully deleted search index entry for entityId: {}", event.getEntityId());
            return;
        }

        Map<String, Object> data = event.getData();
        if (data == null) {
            log.warn("Skipping search sync event for entityId: {} due to null data payload", event.getEntityId());
            return;
        }

        AtomicBoolean isNewEntry = new AtomicBoolean(false);

        SearchIndex index = searchIndexRepository.findById(event.getEntityId())
            .orElseGet(() -> {
                isNewEntry.set(true);
                log.debug("No existing search index found for entityId: {}. Creating new entry.", event.getEntityId());
                return SearchIndex.builder()
                    .id(event.getEntityId())
                    .type(event.getType())
                    .createdAt(Instant.now())
                    .build();
            });

        if (data.containsKey("title") && data.get("title") != null) {
            index.setTitle(data.get("title").toString());
        }

        if (data.containsKey("content") && data.get("content") != null) {
            index.setContent(data.get("content").toString());
        }
        
        if (data.containsKey("icon") && data.get("icon") != null) {
            index.setIcon(data.get("icon").toString());
        }

        if (data.containsKey("slug") && data.get("slug") != null) {
            index.setSlug(data.get("slug").toString());
        }

        if (data.containsKey("score_multiplier") && data.get("score_multiplier") != null) {
            index.setScoreMultiplier(parseDoubleOrDefault(data.get("score_multiplier"), index.getScoreMultiplier()));
        }

        SearchIndex savedIndex = searchIndexRepository.save(index);

        if (isNewEntry.get()) {
            log.info("Successfully created search index entry [ID: {}, Type: {}, Title: {}, Icon: {}, Slug: {}]", 
                savedIndex.getId(), savedIndex.getType(), savedIndex.getTitle(), savedIndex.getIcon(), savedIndex.getSlug());
        } else {
            log.info("Successfully updated search index entry [ID: {}, Type: {}, Title: {}, Icon: {}, Slug: {}]", 
                savedIndex.getId(), savedIndex.getType(), savedIndex.getTitle(), savedIndex.getIcon(), savedIndex.getSlug());
        }
    }

    private double parseDoubleOrDefault(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double value: '{}'. Defaulting to: {}", value, defaultValue);
            return defaultValue;
        }
    }
}