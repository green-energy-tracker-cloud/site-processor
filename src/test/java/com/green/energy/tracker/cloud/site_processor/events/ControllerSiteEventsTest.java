package com.green.energy.tracker.cloud.site_processor.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ControllerSiteEventsTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ControllerSiteEvents controllerSiteEvents;

    private HttpHeaders headers;
    private byte[] eventPayload;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        headers.set("ce-id", "test-event-id-123");
        headers.set("ce-source", "//pubsub.googleapis.com/projects/test-project/topics/test-topic");
        headers.set("ce-type", "com.green.energy.tracker.site.created");
        headers.set("ce-specversion", "1.0");
        headers.set("ce-time", OffsetDateTime.now().toString());
        headers.set("content-type", "application/json");

        eventPayload = "{\"message\":{\"data\":\"dGVzdCBkYXRh\"}}".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void handleSiteEvents_shouldProcessCloudEventSuccessfully() {
        Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
            controllerSiteEvents.handleSiteEvents(headers, eventPayload);

        StepVerifier.create(result)
            .expectComplete()
            .verify();
    }

    @Test
    void handleSiteEvents_shouldReturnEmptyMono() {
        Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
            controllerSiteEvents.handleSiteEvents(headers, eventPayload);

        assertNotNull(result);
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void handleSiteEvents_withDifferentEventType_shouldProcessSuccessfully() {
        headers.set("ce-type", "com.green.energy.tracker.site.updated");

        Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
            controllerSiteEvents.handleSiteEvents(headers, eventPayload);

        StepVerifier.create(result)
            .expectComplete()
            .verify();
    }

    @Test
    void handleSiteEvents_withMinimalHeaders_shouldProcessSuccessfully() {
        headers.clear();
        headers.set("ce-id", "minimal-event");
        headers.set("ce-source", "test-source");
        headers.set("ce-type", "test.type");
        headers.set("ce-specversion", "1.0");

        Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
            controllerSiteEvents.handleSiteEvents(headers, eventPayload);

        StepVerifier.create(result)
            .expectComplete()
            .verify();
    }

    @Test
    void handleSiteEvents_withEmptyPayload_shouldProcessSuccessfully() {
        byte[] emptyPayload = new byte[0];

        Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
            controllerSiteEvents.handleSiteEvents(headers, emptyPayload);

        StepVerifier.create(result)
            .expectComplete()
            .verify();
    }

    @Test
    void handleSiteEvents_withNullPayload_shouldHandleGracefully() {
        assertDoesNotThrow(() -> {
            Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
                controllerSiteEvents.handleSiteEvents(headers, null);
        });
    }

    @Test
    void handleSiteEvents_withCustomAttributes_shouldProcessSuccessfully() {
        headers.set("ce-customattr1", "value1");
        headers.set("ce-customattr2", "value2");

        Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
            controllerSiteEvents.handleSiteEvents(headers, eventPayload);

        StepVerifier.create(result)
            .expectComplete()
            .verify();
    }

    @Test
    void handleSiteEvents_withLargePayload_shouldProcessSuccessfully() {
        StringBuilder largePayload = new StringBuilder("{\"data\":\"");
        for (int i = 0; i < 1000; i++) {
            largePayload.append("test");
        }
        largePayload.append("\"}");

        byte[] largeEventPayload = largePayload.toString().getBytes(StandardCharsets.UTF_8);

        Mono<ResponseEntity<com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto>> result =
            controllerSiteEvents.handleSiteEvents(headers, largeEventPayload);

        StepVerifier.create(result)
            .expectComplete()
            .verify();
    }
}
