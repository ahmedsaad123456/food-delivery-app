package com.example.food_delivery_app.restaurant.mapper;

import com.example.food_delivery_app.restaurant.dto.restaurant.CreateReviewRequest;
import com.example.food_delivery_app.restaurant.dto.restaurant.ReviewResponse;
import com.example.food_delivery_app.restaurant.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurantId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toReviewEntity(CreateReviewRequest request);


    ReviewResponse toReviewResponse(Review review);


}