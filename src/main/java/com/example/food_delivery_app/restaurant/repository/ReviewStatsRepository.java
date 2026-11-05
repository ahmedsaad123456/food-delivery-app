package com.example.food_delivery_app.restaurant.repository;

import com.example.food_delivery_app.restaurant.dto.restaurant.RestaurantRating;
import com.example.food_delivery_app.restaurant.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewStatsRepository {


    // MongoTemplate is used to perform aggregation queries on the MongoDB collection
    private final MongoTemplate mongo;

    // static final fields for the field names used in the aggregation pipeline
    // because they are constants and do not change per instance of the class
    private static final String RESTAURANT_ID_FIELD = "restaurantId";
    private static final String RATING_FIELD = "rating";

    private static final String AVERAGE_RATING_FIELD = "averageRating";

    private static final String REVIEW_COUNT_FIELD = "reviewCount";




//    SELECT restaurant_id, AVG(rating) AS average_rating, COUNT(*) AS review_count
//    FROM reviews
//    GROUP BY restaurant_id
//    ORDER BY average_rating DESC;

    // get all restaurants with their average rating and review count, sorted by average rating in descending order
    public List<RestaurantRating> averageRatingPerRestaurant() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group(RESTAURANT_ID_FIELD)            // group by restaurantId (this value becomes the "_id" of each result)
                        .avg(RATING_FIELD).as(AVERAGE_RATING_FIELD)   // average of the "rating" field
                        .count().as(REVIEW_COUNT_FIELD),          // number of documents in the group
                Aggregation.project(AVERAGE_RATING_FIELD, REVIEW_COUNT_FIELD)
                        .and(RESTAURANT_ID_FIELD).previousOperation(), // rename "_id" back to "restaurantId"
                Aggregation.sort(Sort.Direction.DESC, AVERAGE_RATING_FIELD));

        return mongo.aggregate(aggregation, Review.class, RestaurantRating.class).getMappedResults();
    }

//    SELECT
//    restaurant_id AS restaurantId,
//    AVG(rating) AS averageRating,
//    COUNT(*) AS reviewCount
//    FROM reviews
//    WHERE restaurant_id = 10
//    GROUP BY restaurant_id;
    // get the average rating and review count for a specific restaurant by its ID
    public Optional<RestaurantRating> getAverageRatingAndReviewCountByRestaurantId(Integer restaurantId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(RESTAURANT_ID_FIELD).is(restaurantId)), // filter FIRST: uses the index and shrinks the work
                Aggregation.group(RESTAURANT_ID_FIELD).avg(RATING_FIELD).as(AVERAGE_RATING_FIELD).count().as(REVIEW_COUNT_FIELD),
                Aggregation.project(AVERAGE_RATING_FIELD, REVIEW_COUNT_FIELD).and(RESTAURANT_ID_FIELD).previousOperation());

        return mongo.aggregate(aggregation, Review.class, RestaurantRating.class)
                .getMappedResults().stream().findFirst();
    }
}