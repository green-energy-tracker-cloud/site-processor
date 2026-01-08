package com.green.energy.tracker.cloud.site_processor.service;

import com.green.energy.tracker.cloud.common.v1.GeoLocation;
import com.green.energy.tracker.cloud.site.v1.Site;
import com.green.energy.tracker.cloud.site_processor.model.GeoLocationWrite;
import com.green.energy.tracker.cloud.site_processor.model.SiteMapper;
import com.green.energy.tracker.cloud.site_processor.model.SiteWriteDocument;
import com.green.energy.tracker.cloud.site_processor.repository.SiteRepository;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

        lenient().when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ==================== CREATE TESTS ====================

    @Test
    void create_withValidSite_shouldSaveAndReturnEmptyMono() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(savedDocument));

        Mono<Void> result = siteService.create(site);

        StepVerifier.create(result)
                .verifyComplete();

        verify(siteRepository).save(any(SiteWriteDocument.class));
    }

    @Test
    void create_shouldBuildDocumentWithCorrectSiteFields() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();

        when(siteRepository.save(any(SiteWriteDocument.class))).thenAnswer(invocation -> {
            SiteWriteDocument doc = invocation.getArgument(0);
            assertThat(doc.getId()).isEqualTo("site-123");
            assertThat(doc.getName()).isEqualTo("Test Site");
            assertThat(doc.getUserId()).isEqualTo("user-456");
            assertThat(doc.getAddress()).isEqualTo("123 Test Street");
            assertThat(doc.getLocation().getLatitude()).isEqualTo(40.7128);
            assertThat(doc.getLocation().getLongitude()).isEqualTo(-74.0060);
            return Mono.just(savedDocument);
        });

        Mono<Void> result = siteService.create(site);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void create_whenRepositoryFails_shouldPropagateError() {
        Site site = createTestSite();
        RuntimeException exception = new RuntimeException("Firestore connection error");

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.error(exception));

        Mono<Void> result = siteService.create(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Firestore connection error"))
                .verify();

        verify(siteRepository).save(any(SiteWriteDocument.class));
    }

    @Test
    void create_whenCircuitBreakerOpens_shouldReturnServiceUnavailable() {
        Site site = createTestSite();
        ResponseStatusException circuitBreakerException = new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "Service is temporarily unavailable.");

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(createTestDocument()));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation ->
            Mono.error(circuitBreakerException)
        );

        Mono<Void> result = siteService.create(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof ResponseStatusException &&
                    ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE)
                .verify();
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void update_withExistingSite_shouldFindUpdateAndSave() {
        Site site = createTestSite();
        SiteWriteDocument existingDocument = createTestDocument();
        SiteWriteDocument updatedDocument = createTestDocument();

        when(siteRepository.findById("site-123")).thenReturn(Mono.just(existingDocument));
        when(siteMapper.updateDoc(eq(site), any(SiteWriteDocument.class))).thenReturn(updatedDocument);
        when(siteRepository.save(updatedDocument)).thenReturn(Mono.just(updatedDocument));

        Mono<Void> result = siteService.update(site);

        StepVerifier.create(result)
                .verifyComplete();

        verify(siteRepository).findById("site-123");
        verify(siteMapper).updateDoc(eq(site), any(SiteWriteDocument.class));
        verify(siteRepository).save(updatedDocument);
    }

    @Test
    void update_withNonExistingSite_shouldReturnNotFoundError() {
        Site site = createTestSite();

        when(siteRepository.findById("site-123")).thenReturn(Mono.empty());

        Mono<Void> result = siteService.update(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof ResponseStatusException &&
                    ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.NOT_FOUND &&
                    ((ResponseStatusException) throwable).getReason().equals("Site to update not found"))
                .verify();

        verify(siteRepository).findById("site-123");
        verify(siteRepository, never()).save(any());
    }

    @Test
    void update_whenRepositoryFindByIdFails_shouldPropagateError() {
        Site site = createTestSite();
        RuntimeException exception = new RuntimeException("Database error");

        when(siteRepository.findById("site-123")).thenReturn(Mono.error(exception));

        Mono<Void> result = siteService.update(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Database error"))
                .verify();

        verify(siteRepository).findById("site-123");
    }

    @Test
    void update_whenRepositorySaveFails_shouldPropagateError() {
        Site site = createTestSite();
        SiteWriteDocument existingDocument = createTestDocument();
        SiteWriteDocument updatedDocument = createTestDocument();
        RuntimeException exception = new RuntimeException("Save failed");

        when(siteRepository.findById("site-123")).thenReturn(Mono.just(existingDocument));
        when(siteMapper.updateDoc(eq(site), any(SiteWriteDocument.class))).thenReturn(updatedDocument);
        when(siteRepository.save(updatedDocument)).thenReturn(Mono.error(exception));

        Mono<Void> result = siteService.update(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Save failed"))
                .verify();

        verify(siteRepository).findById("site-123");
        verify(siteRepository).save(updatedDocument);
    }

    @Test
    void update_whenCircuitBreakerOpens_shouldReturnServiceUnavailable() {
        Site site = createTestSite();
        ResponseStatusException circuitBreakerException = new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "Service is temporarily unavailable.");

        when(siteRepository.findById("site-123")).thenReturn(Mono.just(createTestDocument()));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation ->
            Mono.error(circuitBreakerException)
        );

        Mono<Void> result = siteService.update(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof ResponseStatusException &&
                    ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE)
                .verify();
    }

    // ==================== DELETE TESTS ====================

    @Test
    void delete_withExistingSite_shouldFindAndDelete() {
        String siteId = "site-123";
        SiteWriteDocument existingDocument = createTestDocument();

        when(siteRepository.findById(siteId)).thenReturn(Mono.just(existingDocument));
        when(siteRepository.deleteById(siteId)).thenReturn(Mono.empty());

        Mono<Void> result = siteService.delete(siteId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(siteRepository).findById(siteId);
        verify(siteRepository).deleteById(siteId);
    }

    @Test
    void delete_withNonExistingSite_shouldReturnNotFoundError() {
        String siteId = "site-123";

        when(siteRepository.findById(siteId)).thenReturn(Mono.empty());

        Mono<Void> result = siteService.delete(siteId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof ResponseStatusException &&
                    ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.NOT_FOUND &&
                    ((ResponseStatusException) throwable).getReason().equals("Site to delete not found"))
                .verify();

        verify(siteRepository).findById(siteId);
        verify(siteRepository, never()).deleteById(anyString());
    }

    @Test
    void delete_whenRepositoryFindByIdFails_shouldPropagateError() {
        String siteId = "site-123";
        RuntimeException exception = new RuntimeException("Database error");

        when(siteRepository.findById(siteId)).thenReturn(Mono.error(exception));

        Mono<Void> result = siteService.delete(siteId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Database error"))
                .verify();

        verify(siteRepository).findById(siteId);
    }

    @Test
    void delete_whenRepositoryDeleteByIdFails_shouldPropagateError() {
        String siteId = "site-123";
        SiteWriteDocument existingDocument = createTestDocument();
        RuntimeException exception = new RuntimeException("Delete failed");

        when(siteRepository.findById(siteId)).thenReturn(Mono.just(existingDocument));
        when(siteRepository.deleteById(siteId)).thenReturn(Mono.error(exception));

        Mono<Void> result = siteService.delete(siteId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Delete failed"))
                .verify();

        verify(siteRepository).findById(siteId);
        verify(siteRepository).deleteById(siteId);
    }

    @Test
    void delete_whenCircuitBreakerOpens_shouldReturnServiceUnavailable() {
        String siteId = "site-123";
        ResponseStatusException circuitBreakerException = new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "Service is temporarily unavailable.");

        when(siteRepository.findById(siteId)).thenReturn(Mono.just(createTestDocument()));
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation ->
            Mono.error(circuitBreakerException)
        );

        Mono<Void> result = siteService.delete(siteId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                    throwable instanceof ResponseStatusException &&
                    ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE)
                .verify();
    }

    // ==================== HELPER METHODS ====================

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
                .build();
    }
}
