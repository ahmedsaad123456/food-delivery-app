package com.example.food_delivery_app.order.utils;

import com.example.food_delivery_app.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


// listens for OrderPlacedEvent and clears the cart for the user who placed the order
@Component
@RequiredArgsConstructor
public class CartCleanupListener {

    private final CartRepository cartRepository;

    // clears the cart for the user who placed the order after the transaction is committed
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderPlacedEvent event) {
        cartRepository.clear(event.userId());
    }
}