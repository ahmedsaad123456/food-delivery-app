package com.example.food_delivery_app.restaurant.controller;

import com.example.food_delivery_app.common.ApiResponse;
import com.example.food_delivery_app.common.PageResponse;
import com.example.food_delivery_app.common.SimplePageQuery;
import com.example.food_delivery_app.restaurant.dto.restaurant.CreateReviewRequest;
import com.example.food_delivery_app.restaurant.dto.restaurant.RestaurantRating;
import com.example.food_delivery_app.restaurant.dto.restaurant.ReviewResponse;
import com.example.food_delivery_app.restaurant.service.ReviewService;
import com.example.food_delivery_app.restaurant.service.ReviewStatsService;
import com.example.food_delivery_app.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantReviewController {

    private final ReviewService reviewService;
    private final ReviewStatsService reviewStatsService;

    @PostMapping("/{restaurantId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(@PathVariable Integer restaurantId,
                                                    @AuthenticationPrincipal User user,
                                                    @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse saved = reviewService.createReview(restaurantId, user.getId(), request);
        return ApiResponse.ok(saved);
    }

    @GetMapping("/{restaurantId}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> getReviewsByRestaurantId(
            @PathVariable Integer restaurantId,
            @Valid @ParameterObject SimplePageQuery pageQuery) {
        return ApiResponse.ok(reviewService.getReviewsByRestaurantId(restaurantId, pageQuery));
    }

    @GetMapping("/{restaurantId}/rating")
    public ApiResponse<RestaurantRating> getRestaurantRating(@PathVariable Integer restaurantId) {
        return ApiResponse.ok(reviewStatsService.getRestaurantRating(restaurantId));
    }

    @GetMapping("/ratings")
    public ApiResponse<List<RestaurantRating>> getAllRestaurantsRatings() {
        return ApiResponse.ok(reviewStatsService.getAllRatings());
    }
}
