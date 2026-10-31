package com.example.food_delivery_app.menu.repository;

import com.example.food_delivery_app.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface MenuItemRepository extends JpaRepository<MenuItem, Integer>, JpaSpecificationExecutor<MenuItem> {
    List<MenuItem> findByRestaurantId(Integer restaurantId);

    boolean existsByIdAndRestaurantOwnerEmail(Integer id, String email);

    List<MenuItem> findByRestaurantIdOrderBySectionAscNameAsc(Integer restaurantId);
}