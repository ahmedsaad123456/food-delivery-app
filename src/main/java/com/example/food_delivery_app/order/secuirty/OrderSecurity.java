package com.example.food_delivery_app.order.secuirty;

import com.example.food_delivery_app.order.repository.OrderRepository;
import com.example.food_delivery_app.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

    // ROLE_ADMIN can view all orders
    // ORDER_VIEW_OWN can view their own orders
    // ORDER_VIEW_ALL can view all orders of their restaurant
    public boolean canView(Integer orderId, Authentication auth) {
        if (hasAuthority(auth, "ROLE_ADMIN")) return true;
        if (hasAuthority(auth, "ORDER_VIEW_OWN") && isCustomerOf(orderId, auth)) return true;
        return hasAuthority(auth, "ORDER_VIEW_ALL") && isRestaurantOwnerOf(orderId, auth);
    }

    // is the customer of this order
    public boolean isCustomerOf(Integer orderId, Authentication auth) {
        return auth.getPrincipal() instanceof User user
                && orderRepository.existsByIdAndUserId(orderId, user.getId());
    }

    // is the owner of the restaurant of this order
    public boolean isRestaurantOwnerOf(Integer orderId, Authentication auth) {
        return orderRepository.isRestaurantOwner(orderId, auth.getName());     // getName() = email
    }

    // check if the user has a specific authority
    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }
}