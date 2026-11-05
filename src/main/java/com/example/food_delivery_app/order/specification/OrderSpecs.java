package com.example.food_delivery_app.order.specification;

import com.example.food_delivery_app.order.entity.order.Order;
import com.example.food_delivery_app.order.entity.order.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecs {

    private OrderSpecs() {}

    public static Specification<Order> userId(Integer userId) {
        if (userId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<Order> restaurantId(Integer restaurantId) {
        if (restaurantId == null) {
            return Specification.unrestricted();

        }
        return (root, query, cb) -> cb.equal(root.get("restaurantId"), restaurantId);
    }

    public static Specification<Order> status(OrderStatus status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}