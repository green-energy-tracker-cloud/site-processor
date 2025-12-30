package com.green.energy.tracker.cloud.site_processor.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ResilienceConfig {
    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final RetryRegistry retryRegistry;

    @Bean("cbFirestore")
    public ReactiveCircuitBreaker circuitBreakerFirestore(){
        return circuitBreakerFactory.create("firestore");
    }

    @Bean("retryFirestore")
    public Retry retryFirestore() {
        return retryRegistry.retry("firestore");
    }

}
