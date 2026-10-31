package com.example.food_delivery_app.menu.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.menu.dto.CreateMenuItemRequest;
import com.example.food_delivery_app.menu.dto.MenuItemResponse;
import com.example.food_delivery_app.menu.dto.UpdateMenuItemRequest;
import com.example.food_delivery_app.menu.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService service;

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ApiResponse<List<MenuItemResponse>> findMenuItemsByRestaurant(@PathVariable Integer restaurantId) {
        return ApiResponse.ok(service.findMenuItemsByRestaurant(restaurantId));
    }

    @PreAuthorize("hasAuthority('MENU_CREATE') and @restaurantSecurity.isOwner(#restaurantId, authentication)")
    @PostMapping("/restaurants/{restaurantId}/menu-items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MenuItemResponse> createMenuItem(
            @PathVariable Integer restaurantId, @Valid @RequestBody CreateMenuItemRequest request) {
        MenuItemResponse created = service.createMenuItem(restaurantId, request);
        return ApiResponse.ok(created);
    }

    @GetMapping("/menu-items/{id}")
    public ApiResponse<MenuItemResponse> findMenuItemById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findMenuItemById(id));
    }

    @PreAuthorize("hasAuthority('MENU_UPDATE') and @restaurantSecurity.isMenuItemOwner(#id, authentication)")
    @PutMapping("/menu-items/{id}")
    public ApiResponse<MenuItemResponse> updateMenuItem(
            @PathVariable Integer id, @Valid @RequestBody UpdateMenuItemRequest request) {
        return ApiResponse.ok(service.updateMenuItem(id, request));
    }

    @PreAuthorize("hasAuthority('MENU_DELETE') and @restaurantSecurity.isMenuItemOwner(#id, authentication)")
    @DeleteMapping("/menu-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Integer id) {
        service.deleteMenuItem(id);
    }
}