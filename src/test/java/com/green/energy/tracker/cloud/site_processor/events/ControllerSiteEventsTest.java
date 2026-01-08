package com.green.energy.tracker.cloud.site_processor.events;

import com.green.energy.tracker.cloud.site_processor.service.CloudEventManagementService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void handleSiteEvents_withValidCloudEvent_shouldDelegateToServiceAndReturnResponse() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        ResponseEntity<Void> expectedResponse = ResponseEntity.status(HttpStatus.OK).build();

        when(cloudEventManagementService.handleSiteEvents(cloudEvent))
                .thenReturn(Mono.just(expectedResponse));

        Mono<ResponseEntity<Void>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNull();
                })
                .verifyComplete();

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
    }

    @Test
    void handleSiteEvents_whenServiceReturnsEmpty_shouldCompleteEmpty() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .verifyComplete();

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
    }

    @Test
    void handleSiteEvents_whenServiceThrowsError_shouldPropagateError() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        RuntimeException expectedException = new RuntimeException("Service processing error");

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.error(expectedException));

        Mono<ResponseEntity<Void>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Service processing error"))
                .verify();

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
    }

    @Test
    void handleSiteEvents_withAcceptedStatus_shouldReturnCorrectStatusCode() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        ResponseEntity<Void> expectedResponse = ResponseEntity.status(HttpStatus.ACCEPTED).build();

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.just(expectedResponse));

        Mono<ResponseEntity<Void>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
                    assertThat(response.getStatusCodeValue()).isEqualTo(202);
                    assertThat(response.getBody()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void handleSiteEvents_shouldPassEventUnmodifiedToService() throws IOException {
        CloudEvent cloudEvent = createTestCloudEventWithId("specific-event-123");
        ResponseEntity<Void> expectedResponse = ResponseEntity.ok().build();

        when(cloudEventManagementService.handleSiteEvents(cloudEvent))
                .thenReturn(Mono.just(expectedResponse));

        controller.handleSiteEvents(cloudEvent);

        verify(cloudEventManagementService).handleSiteEvents(cloudEvent);
        verifyNoMoreInteractions(cloudEventManagementService);
    }

    @Test
    void handleSiteEvents_withMultipleRequests_shouldHandleIndependently() throws IOException {
        CloudEvent event1 = createTestCloudEventWithId("event-1");
        CloudEvent event2 = createTestCloudEventWithId("event-2");

        ResponseEntity<Void> response1 = ResponseEntity.ok().build();
        ResponseEntity<Void> response2 = ResponseEntity.status(HttpStatus.ACCEPTED).build();

        when(cloudEventManagementService.handleSiteEvents(event1))
                .thenReturn(Mono.just(response1));
        when(cloudEventManagementService.handleSiteEvents(event2))
                .thenReturn(Mono.just(response2));

        Mono<ResponseEntity<Void>> result1 = controller.handleSiteEvents(event1);
        Mono<ResponseEntity<Void>> result2 = controller.handleSiteEvents(event2);

        StepVerifier.create(result1)
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        StepVerifier.create(result2)
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED))
                .verifyComplete();
    }

    @Test
    void handleSiteEvents_shouldReturnResponseWithNoBody() throws IOException {
        CloudEvent cloudEvent = createTestCloudEvent();
        ResponseEntity<Void> expectedResponse = ResponseEntity.status(HttpStatus.OK).build();

        when(cloudEventManagementService.handleSiteEvents(any(CloudEvent.class)))
                .thenReturn(Mono.just(expectedResponse));

        Mono<ResponseEntity<Void>> result = controller.handleSiteEvents(cloudEvent);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.hasBody()).isFalse();
                    assertThat(response.getBody()).isNull();
                })
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
