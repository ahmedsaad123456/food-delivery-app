-- Day 11: webhook idempotency.
-- Stripe may deliver the same event more than once. The PRIMARY KEY on event_id
-- makes the second insert fail, so we know to ignore the duplicate.
CREATE TABLE processed_webhook_events (
    event_id      VARCHAR(255) PRIMARY KEY,      -- Stripe event id, e.g. evt_1Abc...
    event_type    VARCHAR(100) NOT NULL,
    processed_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
