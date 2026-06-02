package com.example.tastetestawdb.integration.user;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class UserGatewayService {

    private static final URI USER_SERVICE_HEALTH = URI.create("http://tastetest-user/actuator/health");

    private final RestTemplate restTemplate;

    public UserGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userFallback")
    @Retry(name = "userService")
    public String pingUserService() {
        return restTemplate.getForObject(USER_SERVICE_HEALTH, String.class);
    }

    public String userFallback(Throwable t) {
        return "user-service-unavailable: " + t.getMessage();
    }
}
