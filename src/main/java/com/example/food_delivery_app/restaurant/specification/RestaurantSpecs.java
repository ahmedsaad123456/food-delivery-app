package com.example.food_delivery_app.restaurant.specification;

import com.example.food_delivery_app.restaurant.entity.Restaurant;
import org.springframework.data.jpa.domain.Specification;

public final class RestaurantSpecs {

    private RestaurantSpecs() {}   // utility class: nobody should instantiate it

    public static Specification<Restaurant> nameContains(String name) {
        if (name == null || name.isBlank()) return null;      // filter not sent -> no condition
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
    }

    public static Specification<Restaurant> category(Integer categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Restaurant> openOnly(Boolean openOnly) {
        if (!Boolean.TRUE.equals(openOnly)) return null;      // false or null -> don't filter
        return (root, query, cb) -> cb.isTrue(root.get("open"));
    }
}