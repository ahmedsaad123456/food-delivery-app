package com.example.food_delivery_app.restaurant.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.restaurant.dto.CreateRestaurantRequest;
import com.example.food_delivery_app.restaurant.dto.RestaurantResponse;
import com.example.food_delivery_app.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.net.URI;

@RestController
@RequestMapping("restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService service;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/restaurants/" + created.id())) // Set the Location header to the URI of the newly created resource (201)
                .body(ApiResponse.ok(created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> getById(@RequestParam Integer id) {
        RestaurantResponse restaurant = service.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(restaurant));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        return ResponseEntity.ok(ApiResponse.ok(service.getAllRestaurants()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable Integer id,
            @Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantResponse updated = service.updateRestaurant(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteRestaurant(@PathVariable Integer id) {
        service.deleteRestaurant(id);
    }


}