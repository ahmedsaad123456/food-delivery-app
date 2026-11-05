package com.example.food_delivery_app.order.events;


import com.example.food_delivery_app.notification.NotificationMessage;
import com.example.food_delivery_app.notification.notifiction_sender.EmailSender;
import com.example.food_delivery_app.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.logging.Logger;


// Uses: EmailSender only (ISP). Runs only AFTER the order is really saved.
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedEmailListener {

    private final EmailSender emailSender;
    private final UserRepository userRepository;



    @Async("orderExecutor") // hand the work to the pool and return immediately
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderPlacedEvent event) {
        // proof of which thread we are on
        log.info("notification for order {} running on thread [{}]",
                event.orderId(), Thread.currentThread().getName());
        userRepository.findById(event.userId()).ifPresent(user ->
                emailSender.send(new NotificationMessage(
                        user.getEmail(),
                        "We got your order #" + event.orderId(),
                        "Thank you! We received your order and the restaurant will start preparing it soon.")));
    }

 }