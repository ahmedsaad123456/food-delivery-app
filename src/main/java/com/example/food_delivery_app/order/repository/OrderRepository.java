package com.example.food_delivery_app.order.repository;

import com.example.food_delivery_app.order.entity.driver.DriverLoad;
import com.example.food_delivery_app.order.entity.order.Order;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    boolean existsByIdAndUserId(Integer id, Integer userId);

    // true if the order's restaurant is owned by this email
    @Query("""
           select count(r) > 0
           from Restaurant r, Order o
           where o.id = :orderId and r.id = o.restaurantId and r.owner.email = :email
           """)
    boolean isRestaurantOwner(@Param("orderId") Integer orderId, @Param("email") String email);


    @Query(value = """
        SELECT driver_id AS driverId, COUNT(*) AS activeOrders
        FROM orders
        WHERE driver_id IN (:driverIds) AND status IN ('PAID', 'PREPARING', 'OUT_FOR_DELIVERY')
        GROUP BY driver_id
        """, nativeQuery = true)
    List<DriverLoad> countActiveOrdersByDriver(@Param("driverIds") Collection<Integer> driverIds);

    @Modifying(clearAutomatically = true) // clearAutomatically = true: clear the persistence context after executing the update query, so that subsequent queries will see the updated data.
    @Query("update Order o set o.driverId = :driverId where o.id = :orderId and o.driverId is null")
    int assignDriver(@Param("orderId") Integer orderId, @Param("driverId") Integer driverId);
}