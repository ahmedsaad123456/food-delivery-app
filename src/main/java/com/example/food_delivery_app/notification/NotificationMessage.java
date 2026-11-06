package com.example.food_delivery_app.notification;

// A Value Object: an invalid message can never exist,
// so every sender receives only VALID messages.
// recipient refers to the email address or phone number of the user to notify
public record NotificationMessage(String recipient, String subject, String body) {

    public NotificationMessage {
        if (recipient == null || recipient.isBlank()) throw new IllegalArgumentException("recipient is required");
        if (body == null || body.isBlank()) throw new IllegalArgumentException("body is required");
        recipient = recipient.strip(); // strip leading and trailing whitespace
        subject = subject == null ? "" : subject.strip();   // subject is optional
    }
}