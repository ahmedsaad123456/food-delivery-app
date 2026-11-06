package com.example.food_delivery_app.payment.outbox;


import com.example.food_delivery_app.payment.entity.OutboxEvent;
import com.example.food_delivery_app.payment.repoistory.OutboxRepository;
import com.example.food_delivery_app.payment.repoistory.ProcessedOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import java.util.Optional;



/**
 * Processes pending outbox events in batches.
 * Delivers each event to its registered handlers,
 * prevents duplicate processing, and handles failures and retries.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final ProcessedOutboxEventRepository processedOutboxEventRepository;
    private final TransactionTemplate tx;

    // Spring injects all beans that implement OutboxEventHandler.
    private final List<OutboxEventHandler> handlers;

    // Maximum number of attempts before an event stops being retried.
    @Value("${outbox.max-attempts:5}")
    private int maxAttempts;

    // Base delay used to calculate the retry interval after a failure.
    @Value("${outbox.backoff-base-seconds:5}")
    private int backoffBaseSeconds;

    // Maximum number of events to process in one batch.
    @Value("${outbox.batch-size:20}")
    private int batchSize;

    // Stores the result of processing one event.
    // found: whether an event was found.
    // failedId: ID of the failed event, if any.
    // error: failure details, if any.
    private record Outcome(boolean found, Long failedId, String error) {}

    /**
     * Processes up to batchSize events.
     * Returns the number of events picked up, including failed ones.
     */
    public int processBatch() {
        int picked = 0;

        // Keep processing until the batch is full or no events are left.
        while (picked < batchSize && processNext()) {
            picked++;
        }

        return picked;
    }

    /**
     * Processes one event.
     * Returns false if there are no pending events.
     */
    public boolean processNext() {

        // Phase 1: claim and process the event inside one transaction.
        Outcome outcome = tx.execute(status -> {

            // Claim the next available event.
            Optional<OutboxEvent> claimed = outboxRepository.claimNext();

            // Stop if there are no pending events.
            if (claimed.isEmpty()) {
                return new Outcome(false, null, null);
            }

            OutboxEvent event = claimed.get();

            try {
                // Deliver the event to its registered handlers.
                deliverEventToHandler(event);

                // Mark the event as published after successful delivery.
                outboxRepository.markPublished(event.id());

                return new Outcome(true, null, null);

            } catch (RuntimeException e) {

                // Roll back phase 1 and release the database row lock.
                status.setRollbackOnly();

                log.warn(
                        "Outbox event {} ({}) failed on attempt {}",
                        event.id(), event.type(), event.attempts() + 1, e
                );

                return new Outcome(true, event.id(), abbreviate(e));
            }
        });

        // Phase 2: record the failure in a separate transaction.
        // Phase 1 was rolled back, so this transaction saves the retry information.
        if (outcome.failedId() != null) {
            tx.executeWithoutResult(s ->
                    outboxRepository.recordFailure(
                            outcome.failedId(),
                            outcome.error(),
                            maxAttempts,
                            backoffBaseSeconds
                    )
            );
        }

        return outcome.found();
    }

    private void deliverEventToHandler(OutboxEvent event) {

        // Find all handlers registered for this event type.
        List<OutboxEventHandler> matching = handlers.stream()
                .filter(h -> h.eventType().equals(event.type()))
                .toList();

        if (matching.isEmpty()) {
            // Fail if no handler is registered for this event type.
            throw new IllegalStateException(
                    "no handler registered for outbox event type " + event.type()
            );
        }

        for (OutboxEventHandler handler : matching) {

            // Check whether this handler has already processed this event.
            if (processedOutboxEventRepository.markProcessed(event.id(), handler.consumerName())) {

                // Process the event only if it has not been handled before.
                // if the handle failed the whole transaction will roll back.
                handler.handle(event);

            } else {
                // Skip the event to avoid duplicate processing.
                log.info(
                        "Event {} already handled by {}, skipping (duplicate delivery)",
                        event.id(), handler.consumerName()
                );
            }
        }
    }

    // Limits the error message to 1000 characters before saving it.
    private static String abbreviate(Exception e) {
        String message = e.getClass().getSimpleName() + ": " + e.getMessage();

        return message.length() > 1000
                ? message.substring(0, 1000)
                : message;
    }
}