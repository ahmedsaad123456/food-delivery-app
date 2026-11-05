package com.example.food_delivery_app.order.repository;


import com.example.food_delivery_app.order.entity.driver.OrderTimelineEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderTimelineRepository extends MongoRepository<OrderTimelineEntry, String> {
    List<OrderTimelineEntry> findByOrderIdOrderByTimestampAsc(Integer orderId); // derived query, like JPA
}