-- Payments: one row per Stripe Checkout Session (an order can have several attempts).
CREATE TABLE payments (
    id                        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id                  INTEGER        NOT NULL REFERENCES orders (id),
    status                    VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
                              CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'EXPIRED', 'REFUNDED')),
    amount                    NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    currency                  CHAR(3)        NOT NULL,
    stripe_session_id         VARCHAR(255)   NOT NULL UNIQUE,
    stripe_payment_intent_id  VARCHAR(255),
    checkout_url              TEXT           NOT NULL,
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_payment_intent ON payments (stripe_payment_intent_id);