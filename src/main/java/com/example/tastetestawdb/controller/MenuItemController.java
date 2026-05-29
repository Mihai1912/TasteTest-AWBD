package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.service.MenuItemService;
import com.example.tastetestawdb.service.dto.MenuItemDto;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/menu-items")
public class MenuItemController implements SecuredRestController {

    public final MenuItemService menuItemService;

    private static final Logger logger = LoggerFactory.getLogger(MenuItemController.class);

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @RequestMapping(path = "/add/{menuId}", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<MenuItemDto> addMenuItem(@Valid @RequestBody MenuItemDto menuItemDto, @PathVariable UUID menuId) {
        try {
            return ResponseEntity.status(201).body(menuItemService.addMenuItem(menuItemDto, menuId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/delete/{menuItemId}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<UUID> deleteMenuItem(@PathVariable UUID menuItemId) {
        try {
            return ResponseEntity.status(200).body(menuItemService.deleteMenuItem(menuItemId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/update/{menuItemId}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<MenuItemDto> updateMenuItem(@Valid @RequestBody MenuItemDto menuItemDto, @PathVariable UUID menuItemId) {
        try {
            return ResponseEntity.status(200).body(menuItemService.updateMenuItem(menuItemDto, menuItemId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/get/{menuItemId}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<MenuItemDto> getMenuItem(@PathVariable UUID menuItemId) {
        try {
            return ResponseEntity.status(200).body(menuItemService.getMenuItem(menuItemId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/get-by-menu/{menuId}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<MenuItemDto>> getMenuItemsByMenu(@PathVariable UUID menuId) {
        try {
            logger.info("Fetching menu items for menu ID: {}", menuId);
            List<MenuItemDto> menuItems = menuItemService.getMenuItemsByMenu(menuId);
            logger.info("Menu items retrieved successfully for menu ID: {}", menuId);
            return ResponseEntity.status(200).body(menuItems);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/get-by-menu/{menuId}/paged", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Page<MenuItemDto>> getMenuItemsByMenuPaged(
            @PathVariable UUID menuId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            return ResponseEntity.status(200).body(menuItemService.getMenuItemsByMenu(menuId, pageable));
        } catch (Exception e) {
            logger.error("Error getting paged menu items", e);
            return ResponseEntity.status(400).body(null);
        }
    }
}
