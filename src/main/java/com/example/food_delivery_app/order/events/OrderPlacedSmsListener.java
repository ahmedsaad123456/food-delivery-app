package com.example.food_delivery_app.order.events;

import com.example.food_delivery_app.notification.NotificationMessage;
import com.example.food_delivery_app.notification.notifiction_sender.SmsSender;
import com.example.food_delivery_app.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Uses: SmsSender only (ISP). Skips users without a phone number.
@Component
@RequiredArgsConstructor
public class OrderPlacedSmsListener {

    private final SmsSender smsSender;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderPlacedEvent event) {
        userRepository.findById(event.userId())
                .filter(user -> user.getPhone() != null && !user.getPhone().isBlank())   // no phone -> skip
                .ifPresent(user -> smsSender.send(new NotificationMessage(
                        user.getPhone(), null, "Your order #" + event.orderId() + " was received.")));
    }
}