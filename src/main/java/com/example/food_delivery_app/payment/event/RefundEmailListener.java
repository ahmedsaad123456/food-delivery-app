package com.example.food_delivery_app.payment.event;

import com.example.food_delivery_app.notification.NotificationMessage;
import com.example.food_delivery_app.notification.notifiction_sender.EmailSender;
import com.example.food_delivery_app.payment.entity.RefundReason;
import com.example.food_delivery_app.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RefundEmailListener {

    private final EmailSender emailSender;           // ISP: email only
    private final UserRepository userRepository;     // adjust the import to your package

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RefundRequestedEvent event) {
        userRepository.findById(event.userId()).ifPresent(user ->
                emailSender.send(new NotificationMessage(
                        user.getEmail(),
                        "Refund for order #" + event.orderId(),
                        explanation(event.reason()) + " We refunded " + event.amount() + " " + event.currency()
                                + ". It usually takes 5 to 10 business days to appear on your card.")));
    }

    private String explanation(RefundReason reason) {
        return switch (reason) {
            case OUT_OF_STOCK -> "Sorry, an item in your order ran out of stock while you were paying, so we had to cancel the order.";
            case ORDER_NOT_PAYABLE -> "This payment was not needed because the order was already paid or cancelled.";
        };
    }
}