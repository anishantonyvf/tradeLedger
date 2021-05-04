package com.tradeledger.cards.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TLConfiguration {

	@Bean
	public RestTemplate initializeRestTemplate() {
		return new RestTemplate();
	}

	@Value("${async.threadpool.size}")
	int asyncThreadPoolCount;

	@Bean
	public ExecutorService asyncExecutorService() {
		return Executors.newFixedThreadPool(asyncThreadPoolCount);
	}

	@Bean
	public CircuitBreaker getDecoratedEligibiltySupplier() {
		CircuitBreakerConfig config = CircuitBreakerConfig.custom().slidingWindowType(SlidingWindowType.COUNT_BASED)
				.slidingWindowSize(1).failureRateThreshold(10.0f).build();
		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
		return registry.circuitBreaker("eligibilityService");
	}

}