package com.green.energy.tracker.cloud.site_processor.events;

import com.green.energy.tracker.cloud.site_processor.service.CloudEventManagementService;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ControllerSiteEvents {

    private final CloudEventManagementService cloudEventManagementService;

    @PostMapping("/events")
    public Mono<ResponseEntity<Void>> handleSiteEvents(@RequestBody CloudEvent event) throws IOException {
        return cloudEventManagementService.handleSiteEvents(event);
    }
}
