package com.example.food_delivery_app.restaurant.repository;

import com.example.food_delivery_app.restaurant.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ReviewRepository extends MongoRepository<Review, String> {
    Page<Review> findByRestaurantIdOrderByCreatedAtDesc(Integer restaurantId, Pageable pageable);
}