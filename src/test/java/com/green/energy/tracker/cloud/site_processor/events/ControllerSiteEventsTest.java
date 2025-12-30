package com.green.energy.tracker.cloud.site_processor.events;

import com.green.energy.tracker.cloud.site_processor.service.CloudEventManagementService;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerSiteEventsTest {

    @Mock
    private CloudEventManagementService cloudEventManagementService;

    private ControllerSiteEvents controller;

    @BeforeEach
    void setUp() {
        controller = new ControllerSiteEvents(cloudEventManagementService);
    }

    @Test
    void handleSiteEvents_withValidEvent_shouldReturnAcceptedResponse() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        SiteResponseDto siteResponseDto = new SiteResponseDto();
        siteResponseDto.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        siteResponseDto.setName("Test Site");

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.just(siteResponseDto));

        Mono<ResponseEntity<SiteResponseDto>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
                    assertThat(response.getBody().getName()).isEqualTo("Test Site");
                })
                .verifyComplete();

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
    }

    @Test
    void handleSiteEvents_whenServiceReturnsEmpty_shouldCompleteEmpty() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<SiteResponseDto>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
    }

    @Test
    void handleSiteEvents_whenServiceThrowsError_shouldPropagateError() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        RuntimeException expectedException = new RuntimeException("Service error");

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.error(expectedException));

        Mono<ResponseEntity<SiteResponseDto>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Service error"))
                .verify();

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
    }

    @Test
    void handleSiteEvents_shouldReturnHttpStatusAccepted() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        SiteResponseDto siteResponseDto = new SiteResponseDto();

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.just(siteResponseDto));

        Mono<ResponseEntity<SiteResponseDto>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
                    assertThat(response.getStatusCodeValue()).isEqualTo(202);
                })
                .verifyComplete();
    }

    @Test
    void handleSiteEvents_shouldInvokeServiceWithProvidedEvent() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        SiteResponseDto siteResponseDto = new SiteResponseDto();

        when(cloudEventManagementService.handleSiteEvents(cloudEvent))
                .thenReturn(Mono.just(siteResponseDto));

        Mono<ResponseEntity<SiteResponseDto>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
    }

    @Test
    void handleSiteEvents_withMultipleRequests_shouldHandleEachIndependently() throws IOException {
        CloudEvent cloudEvent1 = createTestCloudEventWithId("event-1");
        CloudEvent cloudEvent2 = createTestCloudEventWithId("event-2");
        SiteResponseDto response1 = new SiteResponseDto();
        response1.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"));
        SiteResponseDto response2 = new SiteResponseDto();
        response2.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174002"));

        when(cloudEventManagementService.handleSiteEvents(cloudEvent1))
                .thenReturn(Mono.just(response1));
        when(cloudEventManagementService.handleSiteEvents(cloudEvent2))
                .thenReturn(Mono.just(response2));

        Mono<ResponseEntity<SiteResponseDto>> result1 = controller.handleSiteEvents(cloudEvent1);
        Mono<ResponseEntity<SiteResponseDto>> result2 = controller.handleSiteEvents(cloudEvent2);

        StepVerifier.create(result1)
                .assertNext(response -> assertThat(response.getBody().getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")))
                .verifyComplete();

        StepVerifier.create(result2)
                .assertNext(response -> assertThat(response.getBody().getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174002")))
                .verifyComplete();
    }

    private CloudEvent createTestCloudEvent() {
        return createTestCloudEventWithId("test-event-id");
    }

    private CloudEvent createTestCloudEventWithId(String eventId) {
        return CloudEventBuilder.v1()
                .withId(eventId)
                .withSource(URI.create("//pubsub.googleapis.com/projects/test-project/topics/test-topic"))
                .withType("google.cloud.pubsub.topic.v1.messagePublished")
                .withData("application/json", "{\"message\":{}}".getBytes())
                .build();
    }
}
