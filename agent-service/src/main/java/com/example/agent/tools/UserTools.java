package com.example.agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class UserTools {

    private static final Logger logger = LoggerFactory.getLogger(UserTools.class);
    private static final String BASE = "http://tastetest-user/api/v1/user";

    private final RestTemplate restTemplate;

    public UserTools(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Tool(description = "List every user registered in the TasteTest platform with their roles. Admin-only — will return an authorization error for non-admin users.")
    public List<Map<String, Object>> listAllUsers() {
        logger.info("[tool] listAllUsers");
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE + "/all", HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            return List.of(Map.of("error", "This action requires admin privileges."));
        } catch (RestClientException e) {
            logger.warn("listAllUsers failed: {}", e.getMessage());
            return List.of(Map.of("error", "Could not fetch users: " + e.getMessage()));
        }
    }

    @Tool(description = "Get all reviews written by a specific user, identified by UUID. Admin-only — will return an authorization error for non-admin users.")
    public List<Map<String, Object>> getUserReviews(
            @ToolParam(description = "User UUID") String userId) {
        logger.info("[tool] getUserReviews id={}", userId);
        try {
            UUID id = UUID.fromString(userId);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE + "/" + id + "/reviews", HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (IllegalArgumentException e) {
            return List.of(Map.of("error", "Not a valid UUID: " + userId));
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            return List.of(Map.of("error", "This action requires admin privileges."));
        } catch (RestClientException e) {
            logger.warn("getUserReviews failed: {}", e.getMessage());
            return List.of(Map.of("error", "Could not fetch reviews: " + e.getMessage()));
        }
    }
}
