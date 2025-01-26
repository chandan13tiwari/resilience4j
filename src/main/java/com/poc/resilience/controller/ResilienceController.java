package com.poc.resilience.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/resilience")
public class ResilienceController {
    @Autowired
    RestTemplate restTemplate;

    public static int retryCount = 1;
    private static long lastInvocationTime = -1;

    @GetMapping("/circuitBreaker")
    @CircuitBreaker(name = "testCircuitBreaker", fallbackMethod = "fallbackForCircuitBreaker")
    public ResponseEntity<String> doCircuitBreaker() {
        String dummyApiUrl = "http://localhost:8090/dummyApi";
        ResponseEntity<String> response = restTemplate.getForEntity(dummyApiUrl, String.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    public ResponseEntity<String> fallbackForCircuitBreaker(Exception e) {
        return ResponseEntity.ok("dummyApi is down");
    }


    @GetMapping("/retry")
    @Retry(name = "testRetry", fallbackMethod = "fallbackForRetry")
    public ResponseEntity<String> doRetry() {
        // Log the time difference between retries
        long currentTime = System.currentTimeMillis();
        if (lastInvocationTime != -1) {
            long durationMillis = currentTime - lastInvocationTime;
            long durationSeconds = durationMillis / 1000;
            System.out.println("Retry count: " + retryCount++);
            System.out.println("Time since last call: " + durationSeconds + " seconds");
        }

        // Update lastInvocationTime to the current time
        lastInvocationTime = currentTime;

        String dummyApiUrl = "http://localhost:8090/dummyApi";
        ResponseEntity<String> response = restTemplate.getForEntity(dummyApiUrl, String.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    public ResponseEntity<String> fallbackForRetry(Exception e) {
        retryCount = 1;
        return ResponseEntity.ok("dummyApi is down");
    }



    @GetMapping("/rateLimiter")
    @RateLimiter(name = "testRateLimiter", fallbackMethod = "fallbackForRateLimiter")
    public ResponseEntity<String> doRateLimiter() {
        String dummyApiUrl = "http://localhost:8090/dummyApi";
        ResponseEntity<String> response = restTemplate.getForEntity(dummyApiUrl, String.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    public ResponseEntity<String> fallbackForRateLimiter(Exception e) {
        return new ResponseEntity<>("Too Many Requests", HttpStatus.TOO_MANY_REQUESTS);
    }
}
