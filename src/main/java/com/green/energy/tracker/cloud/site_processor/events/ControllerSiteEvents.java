package com.green.energy.tracker.cloud.site_processor.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import io.cloudevents.spring.http.CloudEventHttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ControllerSiteEvents {

    private final ObjectMapper objectMapper;

    @PostMapping("/events")
    public Mono<ResponseEntity<SiteResponseDto>> handleSiteEvents (@RequestHeader HttpHeaders headers, @RequestBody byte[] event){
        var cloudEvent = CloudEventHttpUtils.toReader(headers,()->event).toEvent();
        cloudEvent.getAttributeNames().forEach(a-> log.info(Objects.requireNonNull(cloudEvent.getAttribute(a)).toString()));
        log.info("Received site event: {}", cloudEvent);
        return Mono.empty();
    }
}
