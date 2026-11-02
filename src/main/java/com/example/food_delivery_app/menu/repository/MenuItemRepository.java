package com.example.food_delivery_app.menu.repository;

import com.example.food_delivery_app.menu.entity.MenuItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface MenuItemRepository extends JpaRepository<MenuItem, Integer>, JpaSpecificationExecutor<MenuItem> {
    List<MenuItem> findByRestaurantId(Integer restaurantId);

    boolean existsByIdAndRestaurantOwnerEmail(Integer id, String email);

    List<MenuItem> findByRestaurantIdOrderBySectionAscNameAsc(Integer restaurantId);

    @Query("select distinct m.restaurant.id from MenuItem m where m.id in :ids")
    Set<Integer> findRestaurantIds(@Param("ids") Collection<Integer> ids);
}