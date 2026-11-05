package com.example.food_delivery_app.order.service;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.order.entity.order.Order;
import com.example.food_delivery_app.order.entity.order.OrderStatus;
import com.example.food_delivery_app.order.events.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final ApplicationEventPublisher events;

    /**
     * Runs a domain transition (Order::markPaid, Order::startPreparing ...) and records it in the timeline.
     * Must be called INSIDE a transaction (the timeline listener waits for the commit).
     * consumer is a lambda that calls the transition method on the order, e.g. order::markPaid
     */
    public void orderStatusTransition(Order order, Consumer<Order> transition, String note) {
        OrderStatus old = order.getStatus();
        transition.accept(order); // the Order validates canTransitionTo and throws DomainException if illegal: then NO event is published
        events.publishEvent(new OrderStatusChangedEvent(
                order.getId(), old == null ? null : old.name(), order.getStatus().name(), Instant.now(), note));
    }

    /** For a brand-new order. Call it AFTER save(), so the order already has its id. */
    public void recordCreated(Order order) {
        events.publishEvent(new OrderStatusChangedEvent(
                order.getId(), null, order.getStatus().name(), Instant.now(), Constants.ORDER_CREATED));
    }
}