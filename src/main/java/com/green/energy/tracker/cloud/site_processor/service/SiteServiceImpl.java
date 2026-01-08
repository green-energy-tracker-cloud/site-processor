package com.green.energy.tracker.cloud.site_processor.service;

import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.site_processor.model.GeoLocationWrite;
import com.green.energy.tracker.cloud.site_processor.model.SiteMapper;
import com.green.energy.tracker.cloud.site_processor.model.SiteWriteDocument;
import com.green.energy.tracker.cloud.site_processor.repository.SiteRepository;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService{

    private static final String CB_FIRESTORE_ID = "firestore";
    private final SiteRepository siteRepository;
    private final ReactiveCircuitBreaker cbFirestore;
    private final Retry retryFirestore;
    private final SiteMapper siteMapper;

    @Override
    public Mono<Void> create(Site site) {
        var siteWriteDocument = buildDocument(site);
        return siteRepository.save(siteWriteDocument)
                .transformDeferred(RetryOperator.of(retryFirestore))
                .transformDeferred(mono-> fallbackCircuitBreaker(mono, cbFirestore, CB_FIRESTORE_ID, "save"))
                .log()
                .then();
    }

    @Override
    public Mono<Void> update(Site site) {
        return siteRepository.findById(site.getId())
                .transformDeferred(RetryOperator.of(retryFirestore))
                .transformDeferred(mono-> fallbackCircuitBreaker(mono,cbFirestore,CB_FIRESTORE_ID,"update"))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Site to update not found")))
                .map(siteWriteDocument -> siteMapper.updateDoc(site, siteWriteDocument))
                .flatMap(siteRepository::save)
                .log()
                .then();
    }

    @Override
    public Mono<Void> delete(String siteId) {
        return siteRepository.findById(siteId)
                .transformDeferred(RetryOperator.of(retryFirestore))
                .transformDeferred(mono-> fallbackCircuitBreaker(mono,cbFirestore,CB_FIRESTORE_ID,"delete"))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Site to delete not found")))
                .map(SiteWriteDocument::getId)
                .flatMap(siteRepository::deleteById)
                .log();
    }

    private SiteWriteDocument buildDocument(Site site) {
        return SiteWriteDocument.builder()
                .id(site.getId())
                .name(site.getName())
                .userId(site.getUserId())
                .address(site.getAddress())
                .location(GeoLocationWrite.builder().latitude(site.getLocation().getLatitude()).longitude(site.getLocation().getLongitude()).build())
                .build();
    }

    private <T> Mono<T> fallbackCircuitBreaker(Mono<T> it, ReactiveCircuitBreaker cb, String cbId,  String eventType){
        return cb.run(it, throwable -> {
            log.error("{} Circuit Breaker is open for event type {}. Fallback initiated: {}", cbId, eventType, throwable.getMessage());
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service is temporarily unavailable."));
        });
    }
}
