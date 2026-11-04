package com.example.food_delivery_app.restaurant.repository;

import com.example.food_delivery_app.restaurant.entity.MenuItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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

    // Check and decrement in ONE statement. Returns 1 if deducted (or the item is unlimited), 0 if not enough stock.
    // NULL - qty stays NULL, so unlimited items are untouched.
    // one Atomic query to avoid race conditions when multiple orders are placed at the same time
    // instead read -> check stock -> if enough -> deduct stock, which is not atomic and can lead to negative stock
    @Modifying // modifying query, not a select
    @Query("""
            update MenuItem m set m.stock = m.stock - :qty
            where m.id = :id and (m.stock is null or m.stock >= :qty)
            """)
    int deductStock(@Param("id") Integer id, @Param("qty") int qty);



    // Give stock back (used to undo a partial deduction). NULL + qty stays NULL, so unlimited items are untouched.
    @Modifying
    @Query("update MenuItem m set m.stock = m.stock + :qty where m.id = :id")
    void addStock(@Param("id") Integer id, @Param("qty") int qty);
}