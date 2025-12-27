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

}
