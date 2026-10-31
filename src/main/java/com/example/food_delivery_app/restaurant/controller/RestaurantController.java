package com.example.food_delivery_app.restaurant.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.restaurant.dto.CreateRestaurantRequest;
import com.example.food_delivery_app.restaurant.dto.RestaurantResponse;
import com.example.food_delivery_app.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService service;

    @PreAuthorize("hasAuthority('RESTAURANT_MANAGE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RestaurantResponse> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request, Authentication auth) {
        RestaurantResponse created = service.createRestaurant(request,  auth.getName());   // getName() = email
        return ApiResponse.ok(created);
    }

    @GetMapping
    public ApiResponse<RestaurantResponse> getRestaurantById(@RequestParam Integer id) {
        RestaurantResponse restaurant = service.getRestaurantById(id);
        return ApiResponse.ok(restaurant);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        return ResponseEntity.ok(ApiResponse.ok(service.getAllRestaurants()));
    }

    @PreAuthorize("hasAuthority('RESTAURANT_MANAGE') and @restaurantSecurity.isOwner(#id, authentication)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable Integer id,
            @Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantResponse updated = service.updateRestaurant(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }


    @PreAuthorize("hasAuthority('RESTAURANT_MANAGE') and @restaurantSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteRestaurant(@PathVariable Integer id) {
        service.deleteRestaurant(id);
    }


}