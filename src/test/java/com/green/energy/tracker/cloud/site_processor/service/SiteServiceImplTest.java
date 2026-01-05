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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        // Common mock for circuit breaker to avoid repetition
        when(cbFirestore.run(any(Mono.class), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_withValidSite_shouldSaveAndReturnDto() {
        Site site = createTestSite();
        SiteWriteDocument savedDocument = createTestDocument();
        SiteResponseDto expectedDto = new SiteResponseDto();

        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(savedDocument));
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
            return Mono.just(savedDocument);
        });
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

        Mono<SiteResponseDto> result = siteService.create(site);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Database error"))
                .verify();
    }

    @Test
    void update_withExistingSite_shouldUpdateAndReturnOk() {
        Site siteToUpdate = createTestSite();
        SiteWriteDocument existingDocument = createTestDocument();
        SiteWriteDocument updatedDocument = createTestDocument(); // In a real scenario, this would have updated values

        when(siteRepository.findById(siteToUpdate.getId())).thenReturn(Mono.just(existingDocument));
        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(updatedDocument));

        Mono<ResponseEntity<Void>> result = siteService.update(siteToUpdate);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok().build())
                .verifyComplete();

        verify(siteRepository).findById(siteToUpdate.getId());
        verify(siteRepository).save(any(SiteWriteDocument.class));
    }

    @Test
    void update_withNonExistingSite_shouldReturnNotFound() {
        Site siteToUpdate = createTestSite();

        when(siteRepository.findById(siteToUpdate.getId())).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = siteService.update(siteToUpdate);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();

        verify(siteRepository).findById(siteToUpdate.getId());
    }

    @Test
    void patch_withExistingSite_shouldPatchAndReturnOk() {
        Site siteToPatch = createTestSite();
        SiteWriteDocument existingDocument = createTestDocument();
        SiteWriteDocument patchedDocument = createTestDocument();

        when(siteRepository.findById(siteToPatch.getId())).thenReturn(Mono.just(existingDocument));
        when(siteRepository.save(any(SiteWriteDocument.class))).thenReturn(Mono.just(patchedDocument));

        Mono<ResponseEntity<Void>> result = siteService.patch(siteToPatch);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok().build())
                .verifyComplete();

        verify(siteRepository).findById(siteToPatch.getId());
        verify(siteRepository).save(any(SiteWriteDocument.class));
    }

    @Test
    void patch_withNonExistingSite_shouldReturnNotFound() {
        Site siteToPatch = createTestSite();

        when(siteRepository.findById(siteToPatch.getId())).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = siteService.patch(siteToPatch);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();

        verify(siteRepository).findById(siteToPatch.getId());
    }

    @Test
    void delete_withExistingSite_shouldDeleteAndReturnNoContent() {
        String siteId = "site-123";
        SiteWriteDocument existingDocument = createTestDocument();

        when(siteRepository.findById(siteId)).thenReturn(Mono.just(existingDocument));
        when(siteRepository.deleteById(siteId)).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = siteService.delete(siteId);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.noContent().build())
                .verifyComplete();

        verify(siteRepository).findById(siteId);
        verify(siteRepository).deleteById(siteId);
    }

    @Test
    void delete_withNonExistingSite_shouldReturnNotFound() {
        String siteId = "site-123";

        when(siteRepository.findById(siteId)).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = siteService.delete(siteId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();

        verify(siteRepository).findById(siteId);
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
                .build();
    }
}
