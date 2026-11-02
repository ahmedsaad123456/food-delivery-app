package com.example.food_delivery_app.order.repository;

import com.example.food_delivery_app.order.entity.Order;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    boolean existsByIdAndUserId(Integer id, Integer userId);

    // true if the order's restaurant is owned by this email
    @Query("""
           select count(r) > 0
           from Restaurant r, Order o
           where o.id = :orderId and r.id = o.restaurantId and r.owner.email = :email
           """)
    boolean isRestaurantOwner(@Param("orderId") Integer orderId, @Param("email") String email);
}