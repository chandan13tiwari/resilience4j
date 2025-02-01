package com.poc.resilience.controller;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/resilience")
public class ResilienceController {
    @Autowired
    RestTemplate restTemplate;

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


   @GetMapping("/bulkheadSemaphore")
    @Bulkhead(name = "testBulkheadSemaphore", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "fallbackForBulkheadSemaphore")
    public ResponseEntity<String> doBulkheadSemaphore() {
        System.out.println("Bulkhead semaphore");
        String dummyApiUrl = "http://localhost:8090/dummyApi/dummyBulkhead";
        ResponseEntity<String> response = restTemplate.getForEntity(dummyApiUrl, String.class);
        System.out.println("Thread: " + Thread.currentThread().getName());
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    public ResponseEntity<String> fallbackForBulkheadSemaphore(Exception e) {
        return new ResponseEntity<>("Semaphore::Too many requests", HttpStatus.TOO_MANY_REQUESTS);
    }



    @GetMapping("/bulkheadThreadPool")
    @Bulkhead(name = "testBulkheadThreadPool", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "fallbackForBulkheadThreadPool")
    public CompletableFuture<ResponseEntity<String>> doBulkheadThreadPool() {
        System.out.println("Bulkhead thread pool");
        return CompletableFuture.supplyAsync(() -> {
            String dummyApiUrl = "http://localhost:8090/dummyApi/dummyBulkhead";
            ResponseEntity<String> response = restTemplate.getForEntity(dummyApiUrl, String.class);
            System.out.println("Thread: " + Thread.currentThread().getName());
            System.out.println("response: " + response.getBody());
            return ResponseEntity.ok(response.getBody());
        });
    }

    public CompletableFuture<ResponseEntity<String>> fallbackForBulkheadThreadPool(Throwable throwable) {
        return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body("bulkheadThreadPool::Too Many Requests."));
    }
}
