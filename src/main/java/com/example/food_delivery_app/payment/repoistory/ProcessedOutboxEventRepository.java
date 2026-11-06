package com.example.food_delivery_app.payment.repoistory;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProcessedOutboxEventRepository {

    private final JdbcTemplate jdbc;

    /**
     * true  = first time this consumer sees this event: do the work.
     * false = duplicate delivery: skip it.
     * MANDATORY: the marker must be written in the SAME transaction as the work, so both succeed or both vanish.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean markProcessed(long eventId, String consumer) {
        int inserted = jdbc.update(
                "INSERT INTO processed_outbox_events (event_id, consumer) VALUES (?, ?) ON CONFLICT DO NOTHING",
                eventId, consumer);
        return inserted == 1;
    }
}