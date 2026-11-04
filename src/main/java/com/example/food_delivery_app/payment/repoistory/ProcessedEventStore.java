package com.example.food_delivery_app.payment.repoistory;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;


// Why JDBC ? Because we want to run this in the same transaction as the JPA transaction that processes the event,
// and JPA doesn't have a way to do "insert if not exists" (or "insert ignore") in a portable way.
// So we use JDBC directly for this one operation.
@Component
@RequiredArgsConstructor
public class ProcessedEventStore {

    private final JdbcClient jdbc;

    // true  -> we are the first to see this event, go ahead and process it
    // false -> already processed (or being processed by another transaction that committed): ignore it
    // Runs in the CALLER's transaction (JpaTransactionManager shares its JDBC connection with JdbcClient)
    public boolean markProcessed(String eventId, String eventType) {
        int inserted = jdbc.sql("""
                INSERT INTO processed_webhook_events (event_id, event_type)
                VALUES (:id, :type)
                ON CONFLICT (event_id) DO NOTHING
                """)
                .param("id", eventId)
                .param("type", eventType)
                .update();
        return inserted == 1;
    }
}