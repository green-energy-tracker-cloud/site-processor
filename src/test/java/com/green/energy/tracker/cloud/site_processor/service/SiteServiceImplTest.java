package com.green.energy.tracker.cloud.site_processor.service;

import com.green.energy.tracker.cloud.common.v1.GeoLocation;
import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.site_processor.model.GeoLocationWrite;
import com.green.energy.tracker.cloud.site_processor.model.SiteMapper;
import com.green.energy.tracker.cloud.site_processor.model.SiteWriteDocument;
import com.green.energy.tracker.cloud.site_processor.repository.SiteRepository;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ReactiveCircuitBreaker cbFirestore;

    @Mock
    private SiteMapper siteMapper;

    private Retry retryFirestore;

    private SiteServiceImpl siteService;

    @BeforeEach
    void setUp() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(1)
                .waitDuration(Duration.ofMillis(10))
                .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        retryFirestore = retryRegistry.retry("firestore");

        siteService = new SiteServiceImpl(siteRepository, cbFirestore, retryFirestore, siteMapper);
    }

    @Test
    void create_withValidSite_shouldSaveAndReturnDto() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();
        SiteResponseDto expectedDto = new SiteResponseDto();

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(savedDocument));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(siteMapper.toDto(any(SiteWriteDocument.class))).thenReturn(expectedDto);

        Mono<SiteResponseDto> result = siteService.create(site);

        StepVerifier.create(result)
                .expectNext(expectedDto)
                .verifyComplete();

        verify(siteRepository).save(any(SiteWriteDocument.class));
        verify(siteMapper).toDto(savedDocument);
    }

    @Test
    void create_shouldBuildDocumentWithCorrectFields() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();
        SiteResponseDto expectedDto = new SiteResponseDto();

        when(siteRepository.save(any(SiteWriteDocument.class))).thenAnswer(invocation -> {
            SiteWriteDocument doc = invocation.getArgument(0);
            assertThat(doc.getId()).isEqualTo(site.getId());
            assertThat(doc.getName()).isEqualTo(site.getName());
            assertThat(doc.getUserId()).isEqualTo(site.getUserId());
            assertThat(doc.getAddress()).isEqualTo(site.getAddress());
            assertThat(doc.getLocation().getLatitude()).isEqualTo(site.getLocation().getLatitude());
            assertThat(doc.getLocation().getLongitude()).isEqualTo(site.getLocation().getLongitude());
            assertThat(doc.getCreatedAt()).isNull();
            assertThat(doc.getUpdatedAt()).isNull();
            return Mono.just(savedDocument);
        });
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(siteMapper.toDto(any(SiteWriteDocument.class))).thenReturn(expectedDto);

        Mono<SiteResponseDto> result = siteService.create(site);

        StepVerifier.create(result)
                .expectNext(expectedDto)
                .verifyComplete();

        verify(siteRepository).save(any(SiteWriteDocument.class));
    }

    @Test
    void create_whenRepositoryFails_shouldPropagateError() {
        Site site = createTestSite();
        RuntimeException expectedException = new RuntimeException("Database error");

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.error(expectedException));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<SiteResponseDto> result = siteService.create(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();
    }

    @Test
    void create_whenCircuitBreakerOpen_shouldReturnFallback() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();
        RuntimeException circuitBreakerException = new RuntimeException("Circuit breaker open");

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(savedDocument));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> {
            Function<Throwable, Mono> fallback = invocation.getArgument(1);
            return fallback.apply(circuitBreakerException);
        });

        Mono<SiteResponseDto> result = siteService.create(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        throwable.getMessage().contains("Service is temporarily unavailable"))
                .verify();
    }

    @Test
    void create_shouldApplyCircuitBreaker() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();
        SiteResponseDto expectedDto = new SiteResponseDto();

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(savedDocument));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(siteMapper.toDto(any(SiteWriteDocument.class))).thenReturn(expectedDto);

        Mono<SiteResponseDto> result = siteService.create(site);

        StepVerifier.create(result)
                .expectNext(expectedDto)
                .verifyComplete();

        verify(cbFirestore).run(any(Mono.class), any());
    }

    @Test
    void update_shouldReturnNull() {
        Site site = createTestSite();

        Mono<org.springframework.http.ResponseEntity<Void>> result = siteService.update(site);

        assertThat(result).isNull();
    }

    @Test
    void patch_shouldReturnNull() {
        Site site = createTestSite();

        Mono<org.springframework.http.ResponseEntity<Void>> result = siteService.patch(site);

        assertThat(result).isNull();
    }

    @Test
    void delete_shouldReturnNull() {
        String siteId = "site-123";

        Mono<org.springframework.http.ResponseEntity<Void>> result = siteService.delete(siteId);

        assertThat(result).isNull();
    }

    @Test
    void create_withCircuitBreakerFailure_shouldInvokeFallback() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();
        Throwable circuitBreakerException = new RuntimeException("Circuit breaker triggered");

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(savedDocument));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> {
            Function<Throwable, Mono> fallback = invocation.getArgument(1);
            return fallback.apply(circuitBreakerException);
        });

        Mono<SiteResponseDto> result = siteService.create(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException)
                .verify();

        verify(siteRepository).save(any(SiteWriteDocument.class));
    }

    private Site createTestSite() {
        return Site.newBuilder()
                .setId("site-123")
                .setName("Test Site")
                .setUserId("user-456")
                .setAddress("123 Test Street")
                .setLocation(GeoLocation.newBuilder()
                        .setLatitude(40.7128)
                        .setLongitude(-74.0060)
                        .build())
                .build();
    }

    private SiteWriteDocument createTestDocument() {
        return SiteWriteDocument.builder()
                .id("site-123")
                .name("Test Site")
                .userId("user-456")
                .address("123 Test Street")
                .location(GeoLocationWrite.builder()
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .build())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
}
