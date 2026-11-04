package com.example.food_delivery_app.notification.notifiction_sender;

import com.example.food_delivery_app.notification.NotificationChannel;
import com.example.food_delivery_app.notification.NotificationMessage;


// NotificationSender interface defines the contract for sending notifications through different channels (e.g., SMS, Email, Push).
// Each implementation of this interface will specify the channel it supports and provide the logic to send a notification message.
// ISP (Interface Segregation Principle) is applied here by having separate interfaces for each notification channel (SmsSender, EmailSender, PushSender) that extend the NotificationSender interface.
// This allows clients to depend only on the specific notification sender they need, rather than a general interface that may include methods they don't use.
public interface NotificationSender {
    NotificationChannel channel();
    void send(NotificationMessage message);
}

