package com.poc.resilience.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
}
