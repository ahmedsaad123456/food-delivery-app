package com.example.food_delivery_app.restaurant.service;

import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.common.PageResponse;
import com.example.food_delivery_app.common.SimplePageQuery;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.restaurant.dto.restaurant.CreateReviewRequest;
import com.example.food_delivery_app.restaurant.dto.restaurant.ReviewResponse;
import com.example.food_delivery_app.restaurant.entity.Review;
import com.example.food_delivery_app.restaurant.mapper.ReviewMapper;
import com.example.food_delivery_app.restaurant.repository.RestaurantRepository;
import com.example.food_delivery_app.restaurant.repository.ReviewRepository;
import org.springframework.dao.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviews;
    private final RestaurantRepository restaurants;
    private final ReviewMapper reviewMapper;

    public ReviewResponse createReview(
            Integer restaurantId,
            Integer userId,
            CreateReviewRequest request) {

        if (!restaurants.existsById(restaurantId)) {
            throw new ResourceNotFoundException(
                    "restaurant " , restaurantId
            );
        }

        Review review = reviewMapper.toReviewEntity(request);

        review.setRestaurantId(restaurantId);
        review.setUserId(userId);

        try {
            log.info("Creating review for restaurant {} by user {}", restaurantId, userId);
            return reviewMapper.toReviewResponse( reviews.save(review));
        } catch (DuplicateKeyException e) {
            log.warn("User {} has already reviewed restaurant {}", userId, restaurantId);
            throw new AppException(
                    ErrorCode.USER_REVIEW_ALREADY_EXISTS,userId
            );
        }
    }

    public PageResponse<ReviewResponse> getReviewsByRestaurantId(Integer restaurantId, SimplePageQuery pageQuery) {
        Page<ReviewResponse> page = reviews
                .findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageQuery.toPageable())
                .map(r -> new ReviewResponse(r.getId(), r.getRestaurantId(), r.getUserId(), r.getRating(),
                        r.getComment(), r.getCreatedAt(), r.getExtras()));

        return PageResponse.from(page);
    }
}