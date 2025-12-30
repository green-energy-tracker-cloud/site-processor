package com.green.energy.tracker.cloud.site_processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.site.v1.SiteEventType;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PubSubCloudEventServiceImpl implements CloudEventManagementService{

    private final ObjectMapper objectMapper;
    private final SiteService siteService;
    private final String ATTRIBUTE_ENTITY_ID = "entity_id";
    private final String ATTRIBUTE_EVENT_TYPE = "event_type";

    @Override
    public Mono<SiteResponseDto> handleSiteEvents(CloudEvent event) {
        try {
            MessagePublishedData pubSubEvent = objectMapper.readValue(Objects.requireNonNull(event.getData()).toBytes(), MessagePublishedData.class);
            log.info("PubSub event received: {}", pubSubEvent);
            return handleSiteEventsType(pubSubEvent);
        } catch (IOException e) {
            log.error("Error deserializing Pub/Sub event", e);
            return Mono.error(e);
        }
    }

    private Mono<SiteResponseDto> handleSiteEventsType(MessagePublishedData pubSubEvent) throws InvalidProtocolBufferException {
        var entityId = pubSubEvent.getMessage().getAttributesMap().get(ATTRIBUTE_ENTITY_ID);
        var eventType = SiteEventType.valueOf(pubSubEvent.getMessage().getAttributesMap().get(ATTRIBUTE_EVENT_TYPE));
        switch (eventType) {
            case CREATE -> {
                var site = Site.parseFrom(pubSubEvent.getMessage().getData());
                log.info("Site Event Type: {}", eventType);
                return siteService.create(site);
            }
        }
        return Mono.empty();
    }
}
