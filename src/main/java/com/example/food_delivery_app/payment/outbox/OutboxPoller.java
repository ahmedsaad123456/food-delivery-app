package com.example.food_delivery_app.payment.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
// Creates this bean only when outbox.poller.enabled is true.
// If the property is missing, the bean is created by default.
@ConditionalOnProperty(
        name = "outbox.poller.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxProcessor processor;

    // Runs periodically. Waits 2 seconds after the previous execution finishes.
    // The interval can be customized using outbox.poll-interval-ms.
    // Default interval: 2000 milliseconds (2 seconds).
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:2000}")
    public void poll() {

        try {
            // Processes a batch of pending outbox events.
            // return the number of the events were found and processed
            int picked = processor.processBatch();

            // Log only when events were found and processed.
            if (picked > 0) {
                log.info("outbox: picked {} event(s)", picked);
            }

        } catch (RuntimeException e) {
            // Log the failure without allowing it to interrupt future scheduled executions.
            log.error("outbox poll failed", e);
        }
    }
}