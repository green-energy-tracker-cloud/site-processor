package com.green.energy.tracker.cloud.site_processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.events.cloud.pubsub.v1.PubsubMessage;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.protobuf.ByteString;
import com.green.energy.tracker.cloud.common.v1.GeoLocation;
import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.site.v1.SiteEventType;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PubSubCloudEventServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SiteService siteService;

    private PubSubCloudEventServiceImpl pubSubCloudEventService;

    @BeforeEach
    void setUp() {
        pubSubCloudEventService = new PubSubCloudEventServiceImpl(objectMapper, siteService);
    }

    @Test
    void handleSiteEvents_withCreateEvent_shouldCallSiteServiceCreate() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.CREATE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.CREATE);
        SiteResponseDto expectedResponse = new SiteResponseDto();

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.create(any(Site.class))).thenReturn(Mono.just(expectedResponse));

        Mono<SiteResponseDto> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verify(siteService).create(any(Site.class));
    }

    @Test
    void handleSiteEvents_withIOException_shouldReturnError() throws Exception {
        CloudEvent cloudEvent = createCloudEvent(createTestSite(), SiteEventType.CREATE);
        IOException expectedException = new IOException("Deserialization error");

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenThrow(expectedException);

        Mono<SiteResponseDto> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IOException &&
                        throwable.getMessage().equals("Deserialization error"))
                .verify();

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verify(siteService, never()).create(any(Site.class));
    }

    @Test
    void handleSiteEvents_withInvalidEventData_shouldReturnError() throws Exception {
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId("test-id")
                .withSource(URI.create("test-source"))
                .withType("test-type")
                .withData("application/json", "invalid-json-data".getBytes())
                .build();

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenThrow(new IOException("Invalid JSON"));

        Mono<SiteResponseDto> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectError(IOException.class)
                .verify();
    }

    @Test
    void handleSiteEvents_withUnknownEventType_shouldReturnEmptyMono() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.UNRECOGNIZED);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.UNRECOGNIZED);

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);

        Mono<SiteResponseDto> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .verifyComplete();

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verify(siteService, never()).create(any(Site.class));
    }

    @Test
    void handleSiteEvents_withCreateEventAndServiceError_shouldPropagateError() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.CREATE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.CREATE);
        RuntimeException expectedException = new RuntimeException("Service error");

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.create(any(Site.class))).thenReturn(Mono.error(expectedException));

        Mono<SiteResponseDto> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Service error"))
                .verify();

        verify(siteService).create(any(Site.class));
    }

    private Site createTestSite() {
        return Site.newBuilder()
                .setId("site-123")
                .setName("Test Site")
                .setUserId("user-456")
                .setAddress("123 Test Street")
                .setLocation(GeoLocation.newBuilder()
                        .setLatitude(40.7128)
                        .setLongitude(-74.0060)
                        .build())
                .build();
    }

    private CloudEvent createCloudEvent(Site site, SiteEventType eventType) {
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, eventType);
        byte[] eventData = new byte[0];

        try {
            ObjectMapper mapper = new ObjectMapper();
            eventData = mapper.writeValueAsBytes(messagePublishedData);
        } catch (Exception e) {
            // Ignore for test setup
        }

        return CloudEventBuilder.v1()
                .withId("test-event-id")
                .withSource(URI.create("//pubsub.googleapis.com/projects/test-project/topics/test-topic"))
                .withType("google.cloud.pubsub.topic.v1.messagePublished")
                .withData("application/json", eventData)
                .build();
    }

    private MessagePublishedData createMessagePublishedData(Site site, SiteEventType eventType) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("entity_id", site.getId());
        attributes.put("event_type", eventType.name());

        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFrom(site.toByteArray()))
                .putAllAttributes(attributes)
                .build();

        return MessagePublishedData.newBuilder()
                .setMessage(message)
                .build();
    }
}
