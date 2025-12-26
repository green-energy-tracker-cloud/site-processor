package com.green.energy.tracker.cloud.site_processor.events;

import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
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

@RestController
@Slf4j
@RequiredArgsConstructor
public class ControllerSiteEvents {

    @PostMapping("/events")
    public Mono<ResponseEntity<SiteResponseDto>> handleSiteEvents (@RequestHeader HttpHeaders headers, @RequestBody byte[] event){
        var cloudEvent = CloudEventHttpUtils.toReader(headers,()->event).toEvent();
        log.info("Received site event: {}", cloudEvent);
        return Mono.empty();
    }
}
