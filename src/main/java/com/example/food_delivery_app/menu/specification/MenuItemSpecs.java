package com.example.food_delivery_app.menu.specification;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.menu.entity.MenuItem;
import org.springframework.data.jpa.domain.Specification;


import java.math.BigDecimal;

public final class MenuItemSpecs {

    private MenuItemSpecs() {}   // utility class: no instances

    // Case-insensitive "contains" on the item name
    public static Specification<MenuItem> nameContains(String name) {
        if (name == null || name.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
    }

    // Exact match on the menu section (case-insensitive), e.g. "Burgers"
    public static Specification<MenuItem> section(String section) {
        if (section == null || section.isBlank()) return null;
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("section")), section.trim().toLowerCase());
    }

    // Items of one restaurant. root.get("restaurant").get("id") reads restaurant_id
    // directly from menu_items, so there is no JOIN.
    public static Specification<MenuItem> restaurantId(Integer restaurantId) {
        if (restaurantId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("restaurant").get("id"), restaurantId);
    }

    // Only available items. false or null means "don't filter".
    public static Specification<MenuItem> availableOnly(Boolean availableOnly) {
        if (!Boolean.TRUE.equals(availableOnly)) return null;
        return (root, query, cb) -> cb.isTrue(root.get("available"));
    }

    // Price range. Either bound can be missing (min only, max only, or both).
    public static Specification<MenuItem> priceBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get(Constants.PRICE), min, max);
            }
            return min != null
                    ? cb.greaterThanOrEqualTo(root.get(Constants.PRICE), min)
                    : cb.lessThanOrEqualTo(root.get(Constants.PRICE), max);
        };
    }
}