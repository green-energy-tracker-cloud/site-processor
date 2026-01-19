package com.green.energy.tracker.cloud.site_processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.google.events.cloud.pubsub.v1.PubsubMessage;
import com.google.protobuf.ByteString;
import com.green.energy.tracker.cloud.common.v1.GeoLocation;
import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.site.v1.SiteEventType;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    // ==================== CREATE EVENT TESTS ====================

    @Test
    void handleSiteEvents_withCreateEvent_shouldReturnOkStatusWithNoBody() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.CREATE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.CREATE);

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.create(any(Site.class)))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNull();
                    assertThat(response.hasBody()).isFalse();
                })
                .verifyComplete();

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verify(siteService).create(any(Site.class));
    }

    @Test
    void handleSiteEvents_withCreateEventAndServiceError_shouldPropagateError() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.CREATE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.CREATE);
        RuntimeException serviceException = new RuntimeException("Database error");

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.create(any(Site.class)))
                .thenReturn(Mono.error(serviceException));

        Mono<ResponseEntity<Void>> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Database error"))
                .verify();

        verify(siteService).create(any(Site.class));
    }

    @Test
    void handleSiteEvents_withUpdateEvent_shouldReturnOkStatusWithNoBody() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.UPDATE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.UPDATE);

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.update(any(Site.class)))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNull();
                })
                .verifyComplete();

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verify(siteService).update(any(Site.class));
    }

    @Test
    void handleSiteEvents_withUpdateEventAndServiceError_shouldPropagateError() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.UPDATE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.UPDATE);
        RuntimeException serviceException = new RuntimeException("Update failed");

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.update(any(Site.class)))
                .thenReturn(Mono.error(serviceException));

        Mono<ResponseEntity<Void>> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Update failed"))
                .verify();

        verify(siteService).update(any(Site.class));
    }

    @Test
    void handleSiteEvents_withDeleteEvent_shouldReturnAcceptedStatusWithNoBody() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.DELETE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.DELETE);

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.delete("site-123"))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
                    assertThat(response.getBody()).isNull();
                })
                .verifyComplete();

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verify(siteService).delete("site-123");
    }

    @Test
    void handleSiteEvents_shouldExtractEntityIdFromAttributesForDelete() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.DELETE);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.DELETE);

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);
        when(siteService.delete(anyString()))
                .thenReturn(Mono.empty());

        pubSubCloudEventService.handleSiteEvents(cloudEvent);

        verify(siteService).delete("site-123");
    }

    @Test
    void handleSiteEvents_withUnrecognizedEventType_shouldReturnEmpty() throws Exception {
        Site site = createTestSite();
        CloudEvent cloudEvent = createCloudEvent(site, SiteEventType.UNRECOGNIZED);
        MessagePublishedData messagePublishedData = createMessagePublishedData(site, SiteEventType.UNRECOGNIZED);

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenReturn(messagePublishedData);

        Mono<ResponseEntity<Void>> result = pubSubCloudEventService.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .verifyComplete();

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verifyNoInteractions(siteService);
    }

    @Test
    void handleSiteEvents_whenObjectMapperThrowsIOException_shouldThrowException() throws Exception {
        CloudEvent cloudEvent = createCloudEvent(createTestSite(), SiteEventType.CREATE);
        IOException ioException = new IOException("Deserialization failed");

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenThrow(ioException);

        assertThatThrownBy(() -> pubSubCloudEventService.handleSiteEvents(cloudEvent))
                .isInstanceOf(IOException.class)
                .hasMessage("Deserialization failed");

        verify(objectMapper).readValue(any(byte[].class), eq(MessagePublishedData.class));
        verifyNoInteractions(siteService);
    }

    @Test
    void handleSiteEvents_withInvalidEventData_shouldThrowException() throws Exception {
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId("test-id")
                .withSource(URI.create("test-source"))
                .withType("test-type")
                .withData("application/json", "invalid-json-data".getBytes())
                .build();

        when(objectMapper.readValue(any(byte[].class), eq(MessagePublishedData.class)))
                .thenThrow(new IOException("Invalid JSON"));

        assertThatThrownBy(() -> pubSubCloudEventService.handleSiteEvents(cloudEvent))
                .isInstanceOf(IOException.class)
                .hasMessage("Invalid JSON");
    }

    @Test
    void handleSiteEvents_withNullCloudEventData_shouldThrowNullPointerException() {
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId("test-id")
                .withSource(URI.create("test-source"))
                .withType("test-type")
                .build();

        assertThatThrownBy(() -> pubSubCloudEventService.handleSiteEvents(cloudEvent))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(siteService);
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
