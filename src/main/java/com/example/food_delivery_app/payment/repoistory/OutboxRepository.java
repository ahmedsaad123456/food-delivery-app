package com.example.food_delivery_app.payment.repoistory;


import com.example.food_delivery_app.payment.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OutboxRepository {

    private static final String INSERT_SQL = """
            INSERT INTO outbox_events (aggregate_id, type, payload)
            VALUES (?, ?, CAST(? AS jsonb))
            """;

    // FOR UPDATE SKIP LOCKED: lock the row we take; rows locked by another poller are skipped, not waited for
    private static final String CLAIM_SQL = """
            SELECT id, aggregate_id, type, payload::text AS payload, attempts
            FROM outbox_events
            WHERE status = 'PENDING' AND next_attempt_at <= now()
            ORDER BY id
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """;

    private static final String MARK_PUBLISHED_SQL = """
            UPDATE outbox_events SET status = 'PUBLISHED', published_at = now() WHERE id = ?
            """;

    // attempts + 1, remember the error, FAILED at the limit, otherwise retry later with exponential back-off
    private static final String RECORD_FAILURE_SQL = """
            UPDATE outbox_events
            SET attempts = attempts + 1,
                last_error = ?,
                status = CASE WHEN attempts + 1 >= ? THEN 'FAILED' ELSE 'PENDING' END,
                next_attempt_at = now() + (power(2, attempts) * ? * interval '1 second')
            WHERE id = ?
            """;

    private static final RowMapper<OutboxEvent> EVENT_MAPPER = (rs, i) -> new OutboxEvent(
            rs.getLong("id"),
            rs.getString("aggregate_id"),
            rs.getString("type"),
            rs.getString("payload"),
            rs.getInt("attempts"));

    private final JdbcTemplate jdbc;

    /** Joins the caller's transaction, so the event row commits or rolls back together with the business change. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void insert(String aggregateId, String type, String payloadJson) {
        jdbc.update(INSERT_SQL, aggregateId, type, payloadJson);
    }

    /** Locks and returns the next due event. The lock lives until the surrounding transaction ends. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<OutboxEvent> claimNext() {
        return jdbc.query(CLAIM_SQL, EVENT_MAPPER).stream().findFirst();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markPublished(long id) {
        jdbc.update(MARK_PUBLISHED_SQL, id);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordFailure(long id, String error, int maxAttempts, int backoffBaseSeconds) {
        jdbc.update(RECORD_FAILURE_SQL, error, maxAttempts, backoffBaseSeconds, id);
    }
}