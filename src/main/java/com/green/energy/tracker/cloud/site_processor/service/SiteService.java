package com.green.energy.tracker.cloud.site_processor.service;

import com.green.energy.tracker.cloud.site.v1.Site;
import reactor.core.publisher.Mono;

public interface SiteService {
    Mono<Void> create(Site site);
    Mono<Void> update(Site site);
    Mono<Void> delete(String siteId);
}
