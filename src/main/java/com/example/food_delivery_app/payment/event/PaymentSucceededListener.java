package com.example.food_delivery_app.payment.event;

import com.example.food_delivery_app.notification.NotificationMessage;
import com.example.food_delivery_app.notification.notifiction_sender.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSucceededListener {

    private final EmailSender emailSender;           // ISP: email only


    // @EventListener (NOT AFTER_COMMIT): it runs inside the webhook transaction,
    // so "payment SUCCEEDED + order PAID + stock deducted + event id recorded" are saved ALL together or NOT AT ALL.
    @EventListener
    public void on(PaymentSucceededEvent event) {
        emailSender.send(new NotificationMessage(
                event.userId().toString(),
                "Payment Succeeded",
                "Your payment for order " + event.orderId() + " has succeeded. Thank you for your purchase!"
        ));
    }
}