package com.example.food_delivery_app.order.controller;


import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.order.dto.driver.CreateDriverRequest;
import com.example.food_delivery_app.order.dto.driver.DriverResponse;
import com.example.food_delivery_app.order.dto.driver.UpdateDriverLocationRequest;
import com.example.food_delivery_app.order.dto.driver.UpdateDriverStatusRequest;
import com.example.food_delivery_app.order.entity.driver.DriverStatus;
import com.example.food_delivery_app.order.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DRIVER_MANAGE')")
    public ApiResponse<DriverResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        return ApiResponse.ok(service.createDriver(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DriverResponse> getDriverById(@PathVariable Integer id) { return ApiResponse.ok(service.getDriverById(id)); }

    @GetMapping
    public ApiResponse<List<DriverResponse>> getAllDriversByStatus(@RequestParam(required = false) DriverStatus status) { return ApiResponse.ok(service.getAllDriversByStatus(status)); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('DRIVER_UPDATE')")
    public ApiResponse<DriverResponse> updateDriverStatus(@PathVariable Integer id, @Valid @RequestBody UpdateDriverStatusRequest request) {
        return ApiResponse.ok(service.updateDriverStatus(id, request.status()));
    }

    @PatchMapping("/{id}/location")
    @PreAuthorize("hasAuthority('DRIVER_UPDATE')")
    public ApiResponse<DriverResponse> updateDriverLocation(@PathVariable Integer id, @Valid @RequestBody UpdateDriverLocationRequest request) {
        return ApiResponse.ok(service.updateDriverLocation(id, request.latitude(), request.longitude()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('DRIVER_MANAGE')")
    public void deleteDriver(@PathVariable Integer id) {
        service.deleteDriver(id);
    }
}