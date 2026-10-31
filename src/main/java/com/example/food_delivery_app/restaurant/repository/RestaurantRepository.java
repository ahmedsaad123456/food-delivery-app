package com.example.food_delivery_app.restaurant.repository;

import com.example.food_delivery_app.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {

    boolean existsByIdAndOwnerEmail(Integer id, String email);

}
