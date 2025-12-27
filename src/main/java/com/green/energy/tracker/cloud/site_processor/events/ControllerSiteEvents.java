package com.green.energy.tracker.cloud.site_processor.events;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@Slf4j
@RequiredArgsConstructor
public class ControllerSiteEvents {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @PostMapping("/events")
    public Mono<ResponseEntity<SiteResponseDto>> handleSiteEvents(@RequestBody CloudEvent event) {
        return Mono.fromCallable(() -> {
            event.getAttributeNames().forEach(a->{
                log.info("Attribute: {} - Value: {}", a, event.getAttribute(a));
            });
            log.info(event.toString());
            return ResponseEntity.ok().build();
        });
    }
}
