package com.green.energy.tracker.cloud.site_processor.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResilienceConfigTest {

    @Mock
    private ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private ReactiveCircuitBreaker mockCircuitBreaker;

    @Mock
    private Retry mockRetry;

    private ResilienceConfig resilienceConfig;

    @BeforeEach
    void setUp() {
        resilienceConfig = new ResilienceConfig(circuitBreakerFactory, retryRegistry);
    }

    @Test
    void circuitBreakerFirestore_shouldCreateCircuitBreakerWithFirestoreName() {
        when(circuitBreakerFactory.create("firestore")).thenReturn(mockCircuitBreaker);
        ReactiveCircuitBreaker result = resilienceConfig.circuitBreakerFirestore();
        assertThat(result).isNotNull().isSameAs(mockCircuitBreaker);
        verify(circuitBreakerFactory).create("firestore");
    }

    @Test
    void retryFirestore_shouldCreateRetryWithFirestoreName() {
        when(retryRegistry.retry("firestore")).thenReturn(mockRetry);
        Retry result = resilienceConfig.retryFirestore();
        assertThat(result).isNotNull().isSameAs(mockRetry);
        verify(retryRegistry).retry("firestore");
    }

    @Test
    void circuitBreakerFirestore_shouldReturnSameInstanceOnMultipleCalls() {
        when(circuitBreakerFactory.create("firestore")).thenReturn(mockCircuitBreaker);
        ReactiveCircuitBreaker firstCall = resilienceConfig.circuitBreakerFirestore();
        ReactiveCircuitBreaker secondCall = resilienceConfig.circuitBreakerFirestore();
        assertThat(firstCall).isSameAs(secondCall);
    }

    @Test
    void retryFirestore_shouldReturnSameInstanceOnMultipleCalls() {
        when(retryRegistry.retry("firestore")).thenReturn(mockRetry);
        Retry firstCall = resilienceConfig.retryFirestore();
        Retry secondCall = resilienceConfig.retryFirestore();
        assertThat(firstCall).isSameAs(secondCall);
    }
}
