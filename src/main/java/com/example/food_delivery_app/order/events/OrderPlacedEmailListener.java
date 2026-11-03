package com.example.food_delivery_app.order.events;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


// Uses: EmailSender only (ISP). Runs only AFTER the order is really saved.
@Component
@RequiredArgsConstructor
public class OrderPlacedEmailListener {

//    private final EmailSender emailSender; // need an implementation of EmailSender to send email notifications
//    private final UserRepository userRepository;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void on(OrderPlacedEvent event) {
//        userRepository.findById(event.userId()).ifPresent(user ->
//                emailSender.send(new NotificationMessage(
//                        user.getEmail(),
//                        "We got your order #" + event.orderId(),
//                        "Thank you! We received your order and the restaurant will start preparing it soon.")));
//    }
}