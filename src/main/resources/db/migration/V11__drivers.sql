-- Day 14: delivery drivers and order assignment.
CREATE TABLE drivers (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    phone       VARCHAR(30)   NOT NULL UNIQUE,
    status      VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE'
                CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE')),
    latitude    NUMERIC(9, 6),
    longitude   NUMERIC(9, 6),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- The driver assigned to an order (null until one is assigned).
ALTER TABLE orders
    ADD COLUMN driver_id INTEGER REFERENCES drivers (id) ON DELETE SET NULL;
