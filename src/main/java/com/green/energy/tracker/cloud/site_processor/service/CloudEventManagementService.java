package com.green.energy.tracker.cloud.site_processor.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import io.cloudevents.CloudEvent;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface CloudEventManagementService {
    Mono<SiteResponseDto> handleSiteEvents(CloudEvent event) throws IOException;
}
