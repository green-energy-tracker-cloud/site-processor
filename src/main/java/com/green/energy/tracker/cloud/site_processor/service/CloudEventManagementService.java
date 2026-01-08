package com.green.energy.tracker.cloud.site_processor.service;

import io.cloudevents.CloudEvent;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface CloudEventManagementService {
    Mono<ResponseEntity<Void>> handleSiteEvents(CloudEvent event) throws IOException;
}
