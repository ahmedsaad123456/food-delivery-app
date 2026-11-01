package com.example.food_delivery_app.cart.service;

import com.example.food_delivery_app.cart.dto.CartItemResponse;
import com.example.food_delivery_app.cart.dto.CartResponse;
import com.example.food_delivery_app.cart.pricing.discount_strategy.DiscountType;
import com.example.food_delivery_app.cart.pricing.PriceSummary;
import com.example.food_delivery_app.cart.pricing.PricingService;
import com.example.food_delivery_app.cart.repository.CartRepository;
import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.common.Money;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.menu.entity.MenuItem;
import com.example.food_delivery_app.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CartService {
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final PricingService pricingService;

    public void addToCart(Integer userId, Integer menuItemId, int quantity) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MENU_ITEM_ERROR, menuItemId));
        if (!item.isAvailable()) {                       // use getAvailable() if your field is Boolean
            throw new AppException(ErrorCode.MENU_ITEM_UNAVAILABLE, item.getName());
        }
        long newQuantity = cartRepository.increment(userId, menuItemId, quantity);
        if (newQuantity > Constants.MAX_QUANTITY) {                // repeated adds must not pass the limit
            cartRepository.setQuantity(userId, menuItemId, Constants.MAX_QUANTITY);
        }

    }

    public void removeFromCart(Integer userId, Integer menuItemId) {
        cartRepository.remove(userId, menuItemId);       // idempotent: removing twice is not an error
    }

    public void updateQuantity(Integer userId, Integer menuItemId, int quantity) {
        if (!cartRepository.contains(userId, menuItemId)) {
            throw new ResourceNotFoundException(Constants.MENU_ITEM_ERROR, menuItemId);
        }
        if (quantity == 0) {
            cartRepository.remove(userId, menuItemId);
        } else {
            cartRepository.setQuantity(userId, menuItemId, Math.min(quantity, Constants.MAX_QUANTITY));
        }
    }

    public CartResponse getCart(Integer userId) {
        Map<Integer, Integer> quantities = cartRepository.findAll(userId);
        if (quantities.isEmpty()) {
            return new CartResponse(List.of(), Money.zero(Money.EGP));
        }

        // ONE query for all items (findAllById) instead of one query per cart line (N+1)
        Map<Integer, MenuItem> items = menuItemRepository.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity())); // key = menuItemId, value = MenuItem

        List<CartItemResponse> lines = quantities.entrySet().stream()
                .filter(e -> items.containsKey(e.getKey()))      // item deleted from the menu meanwhile
                .map(e -> {
                    MenuItem item = items.get(e.getKey());
                    Money unitPrice = Money.egpMoney(item.getPrice());
                    int quantity = e.getValue();
                    return new CartItemResponse(item.getId(), item.getName(),
                            unitPrice, quantity, unitPrice.multiply(quantity));
                })
                .sorted(Comparator.comparing(CartItemResponse::name))
                .toList();

        // reduce: start from zero, add every line total
        Money subtotal = lines.stream()
                .map(CartItemResponse::lineTotal)// get the line total for each item
                .reduce(Money.zero(Money.EGP), Money::add);// add them all together, starting from zero

        return new CartResponse(lines, subtotal);
    }

    public PriceSummary summary(Integer userId, DiscountType type, BigDecimal discountValue) {
        return pricingService.price(getCart(userId).subtotal(), type, discountValue);
    }

    }
