package com.example.food_delivery_app.order.events;


import com.example.food_delivery_app.order.entity.driver.OrderTimelineEntry;
import com.example.food_delivery_app.order.repository.OrderTimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTimelineListener {

    private final OrderTimelineRepository repository;

    // fallbackExecution=true is important: if the transaction fails, we still want to log the timeline entry
    // it means that if the transaction is rolled back, the event listener will still be executed.
    // This is useful for logging or auditing purposes, as it allows you to capture events even if the transaction fails.
    @Async("orderExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            repository.save(new OrderTimelineEntry(
                    null, event.orderId(), event.toStatus(), event.occurredAt(), event.note()));
        } catch (RuntimeException e) {
            // A timeline failure must never break the order flow (the order is already committed in Postgres)
            log.error("Could not write timeline entry for order {} status {}", event.orderId(), event.toStatus(), e);
        }
    }
}