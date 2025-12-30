package com.green.energy.tracker.cloud.site_processor.service;

import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface SiteService {
    Mono<SiteResponseDto> create(Site site);
    Mono<ResponseEntity<Void>> update(Site site);
    Mono<ResponseEntity<Void>> patch(Site site);
    Mono<ResponseEntity<Void>> delete(String siteId);
}
