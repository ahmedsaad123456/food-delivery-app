package com.example.food_delivery_app.restaurant.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "reviews")
@CompoundIndex(name = "one_review_per_user_per_restaurant", def = "{'userId': 1, 'restaurantId': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    private String id;

    @Indexed                    // we always search reviews by restaurant
    private Integer restaurantId; // reference to restaurants.id in Postgres (not enforced)

    private Integer userId;     // who wrote it
    private int rating;         // 1..5 (validated in the DTO, see below)
    private String comment;
    private Instant createdAt = Instant.now();

    // The flexible part: ANY extra fields (tags, dishRatings, deliveryRating, ...).
    // Stored inside the same document, nested maps and lists are fine.
    private Map<String, Object> extras = new HashMap<>();
}