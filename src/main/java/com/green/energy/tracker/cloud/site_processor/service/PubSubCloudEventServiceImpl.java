package com.green.energy.tracker.cloud.site_processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.site.v1.SiteEventType;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private static final String attributeEntityId = "entity_id";
    private static final String attributeEventType = "event_type";

    @Override
    public Mono<ResponseEntity<Void>> handleSiteEvents(CloudEvent event) throws IOException {
        var pubSubEvent = objectMapper.readValue(Objects.requireNonNull(event.getData()).toBytes(), MessagePublishedData.class);
        log.info("PubSub event received: {}", pubSubEvent);
        return handleSiteEventsType(pubSubEvent);
    }

    private Mono<ResponseEntity<Void>> handleSiteEventsType(MessagePublishedData pubSubEvent) throws InvalidProtocolBufferException {
        var entityId = pubSubEvent.getMessage().getAttributesMap().get(attributeEntityId);
        var eventType = SiteEventType.valueOf(pubSubEvent.getMessage().getAttributesMap().get(attributeEventType));
        switch (eventType) {
            case CREATE -> {
                return siteService.create(Site.parseFrom(pubSubEvent.getMessage().getData()))
                        .then(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));
            }
            case UPDATE -> {
                return siteService.update(Site.parseFrom(pubSubEvent.getMessage().getData()))
                        .then(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));
            }
            case DELETE -> {
                return siteService.delete(entityId)
                        .then(Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).build()));
            }
        }
        return Mono.empty();
    }
}
