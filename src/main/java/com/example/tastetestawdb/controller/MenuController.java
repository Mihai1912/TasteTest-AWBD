package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.service.MenuService;
import com.example.tastetestawdb.service.dto.MenuDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController implements SecuredRestController {
    public final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<MenuDto> addMenu(@RequestParam String name,
                                           @RequestParam String restaurantName) {
        try {
            return ResponseEntity.status(201).body(menuService.addMenu(name, restaurantName));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<UUID> deleteMenu(@PathVariable UUID id) {
        try {
            return ResponseEntity.status(200).body(menuService.deleteMenu(id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/update/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<MenuDto> updateMenu(@PathVariable UUID id,
                                              @RequestParam String name) {
        try {
            return ResponseEntity.status(200).body(menuService.updateMenu(id, name));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/get/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<MenuDto> getMenu(@PathVariable UUID id) {
        try {
            return ResponseEntity.status(200).body(menuService.getMenu(id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/getRestaurantMenus/{restaurantId}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<MenuDto>> getRestaurantMenus(@PathVariable UUID restaurantId) {
        try {
            return ResponseEntity.status(200).body(menuService.getRestaurantMenus(restaurantId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/getRestaurantMenus/{restaurantId}/paged", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Page<MenuDto>> getRestaurantMenusPaged(
            @PathVariable UUID restaurantId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            return ResponseEntity.status(200).body(menuService.getRestaurantMenus(restaurantId, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }
}
