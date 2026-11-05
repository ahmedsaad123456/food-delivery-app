package com.example.food_delivery_app.order.events;


import java.time.Instant;

// order status change event, emitted when an order's status changes
// occurredAt is the timestamp of when the status change occurred
public record OrderStatusChangedEvent(Integer orderId, String fromStatus, String toStatus,
                                      Instant occurredAt, String note) {}