package com.example.food_delivery_app.order.dto.order;

import java.time.Instant;

public record TimelineEntryResponse(String status, Instant timestamp, String note) {}