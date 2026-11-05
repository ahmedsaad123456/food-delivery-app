package com.example.food_delivery_app.restaurant.service;

import com.example.food_delivery_app.restaurant.dto.restaurant.RestaurantRating;
import com.example.food_delivery_app.restaurant.repository.ReviewStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewStatsService {

    private final ReviewStatsRepository stats;

    public RestaurantRating getRestaurantRating(Integer restaurantId) {
        return stats.getAverageRatingAndReviewCountByRestaurantId(restaurantId)
                .orElseGet(() -> new RestaurantRating(restaurantId, 0.0, 0)); // no reviews yet
    }

    public List<RestaurantRating> getAllRatings() {
        return stats.averageRatingPerRestaurant();
    }
}