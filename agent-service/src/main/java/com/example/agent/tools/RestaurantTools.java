package com.example.agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class RestaurantTools {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantTools.class);
    private static final String BASE = "http://tastetest-restaurant/api/v1/restaurant";

    private final RestTemplate restTemplate;

    public RestaurantTools(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Tool(description = "List every restaurant in the TasteTest catalog. Returns name, cuisine, location, and id. Use this when the user wants to browse, search, or compare restaurants.")
    public List<Map<String, Object>> listRestaurants() {
        logger.info("[tool] listRestaurants");
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE + "/getAll", HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (RestClientException e) {
            logger.warn("listRestaurants failed: {}", e.getMessage());
            return List.of(Map.of("error", "Could not fetch restaurants: " + e.getMessage()));
        }
    }

    @Tool(description = "Return the top-rated restaurants on TasteTest, ordered by average rating. Use when the user asks for the best, most popular, or highest-rated places.")
    public List<Map<String, Object>> getTopRatedRestaurants() {
        logger.info("[tool] getTopRatedRestaurants");
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    BASE + "/top-rated", HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (RestClientException e) {
            logger.warn("getTopRatedRestaurants failed: {}", e.getMessage());
            return List.of(Map.of("error", "Could not fetch top-rated restaurants: " + e.getMessage()));
        }
    }

    @Tool(description = "Get full details of a single restaurant by its UUID. Use after listRestaurants when the user asks for more info on a specific restaurant.")
    public Map<String, Object> getRestaurantDetails(
            @ToolParam(description = "Restaurant UUID, e.g. '550e8400-e29b-41d4-a716-446655440000'") String restaurantId) {
        logger.info("[tool] getRestaurantDetails id={}", restaurantId);
        try {
            UUID id = UUID.fromString(restaurantId);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    BASE + "/get/" + id, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody() == null ? Map.of("error", "Restaurant not found") : response.getBody();
        } catch (IllegalArgumentException e) {
            return Map.of("error", "Not a valid UUID: " + restaurantId);
        } catch (RestClientException e) {
            logger.warn("getRestaurantDetails failed: {}", e.getMessage());
            return Map.of("error", "Could not fetch restaurant: " + e.getMessage());
        }
    }

    @Tool(description = "Get the average rating (0-5) for a specific restaurant by UUID.")
    public String getAverageRating(
            @ToolParam(description = "Restaurant UUID") String restaurantId) {
        logger.info("[tool] getAverageRating id={}", restaurantId);
        try {
            UUID id = UUID.fromString(restaurantId);
            Double rating = restTemplate.getForObject(BASE + "/getRatings/" + id, Double.class);
            return rating == null ? "No rating yet" : String.format("%.2f / 5", rating);
        } catch (IllegalArgumentException e) {
            return "Not a valid UUID: " + restaurantId;
        } catch (RestClientException e) {
            logger.warn("getAverageRating failed: {}", e.getMessage());
            return "Could not fetch rating: " + e.getMessage();
        }
    }

    @Tool(description = "Look up a restaurant's UUID given its name. Useful when the user mentions a restaurant by name and you need its id for another tool.")
    public String findRestaurantIdByName(
            @ToolParam(description = "Restaurant name (case-sensitive, must match exactly)") String name) {
        logger.info("[tool] findRestaurantIdByName name={}", name);
        try {
            UUID id = restTemplate.getForObject(BASE + "/getRestaurantId/" + name, UUID.class);
            return id == null ? "No restaurant named '" + name + "'" : id.toString();
        } catch (RestClientException e) {
            logger.warn("findRestaurantIdByName failed: {}", e.getMessage());
            return "Could not find restaurant: " + e.getMessage();
        }
    }
}
