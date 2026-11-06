-- Transactional outbox: events are written in the SAME transaction as the business change,
-- and a background poller delivers them later (at-least-once).
CREATE TABLE outbox_events (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_id    VARCHAR(100) NOT NULL,                 -- e.g. the order id this event is about
    type            VARCHAR(100) NOT NULL,                 -- e.g. 'PAYMENT_SUCCEEDED'
    payload         JSONB        NOT NULL,                 -- the event data
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    attempts        INTEGER      NOT NULL DEFAULT 0,
    last_error      TEXT,                                  -- why the last attempt failed
    next_attempt_at TIMESTAMPTZ  NOT NULL DEFAULT now(),   -- retry back-off: do not retry before this moment
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);

-- The poller only ever looks at PENDING rows, so index only those (small and fast even with millions of old rows).
CREATE INDEX idx_outbox_pending ON outbox_events (id) WHERE status = 'PENDING';

-- Idempotent consumers: "consumer X already handled event Y".
CREATE TABLE processed_outbox_events (
    event_id     INTEGER       NOT NULL,
    consumer     VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer)
);