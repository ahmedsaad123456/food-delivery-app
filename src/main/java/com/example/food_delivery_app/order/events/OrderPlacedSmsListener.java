package com.example.food_delivery_app.order.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


// Uses: SmsSender only (ISP). Skips users without a phone number.
@Component
@RequiredArgsConstructor
public class OrderPlacedSmsListener {

//    private final SmsSender smsSender; // need an implementation of SmsSender to send email notifications
//    private final UserRepository userRepository;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void on(OrderPlacedEvent event) {
//        userRepository.findById(event.userId())
//                .filter(user -> user.getPhone() != null && !user.getPhone().isBlank())   // no phone -> skip
//                .ifPresent(user -> smsSender.send(new NotificationMessage(
//                        user.getPhone(), null, "Your order #" + event.orderId() + " was received.")));
//    }
}