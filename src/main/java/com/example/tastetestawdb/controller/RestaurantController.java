package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.service.RestaurantService;
import com.example.tastetestawdb.service.dto.RestaurantDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurant")
public class RestaurantController implements SecuredRestController{
    private final RestaurantService restaurantService;

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<RestaurantDto> addRestaurant(@RequestBody RestaurantDto restaurantDto) {
        try {
            return ResponseEntity.status(201).body(restaurantService.addRestaurant(restaurantDto));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteRestaurant(@PathVariable UUID id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.status(200).body("Restaurant " + id + " deleted");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/update/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<RestaurantDto> updateRestaurant(@PathVariable UUID id, @RequestBody RestaurantDto restaurantDto) {
        try {
            return ResponseEntity.status(200).body(restaurantService.updateRestaurant(id, restaurantDto));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/get/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable UUID id) {
        try {
            return ResponseEntity.status(200).body(restaurantService.getRestaurant(id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/getAll", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        try {
            return ResponseEntity.status(200).body(restaurantService.getAllRestaurants());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/getRestaurantId/{name}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<UUID> getRestaurantId(@PathVariable String name) {
        try {
            return ResponseEntity.status(200).body(restaurantService.getRestaurantId(name));
        } catch (Exception e) {
            logger.error("Error getting restaurant id for restaurant with name: " + name, e);
            return ResponseEntity.status(400).body(null);
        }
    }
}
