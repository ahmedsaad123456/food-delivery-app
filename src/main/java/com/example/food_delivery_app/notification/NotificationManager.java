package com.example.food_delivery_app.notification;

import com.example.food_delivery_app.notification.notifiction_sender.EmailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificationManager implements EmailSender {
    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        new SendResult(channel(), 3, message.body());
    }
}
