-- Day 8: Orders and Order Items

CREATE TABLE orders (
    id                INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           INTEGER NOT NULL REFERENCES users (id),
    restaurant_id     INTEGER NOT NULL REFERENCES restaurants (id),

    status            VARCHAR(30) NOT NULL DEFAULT 'PENDING'
                      CHECK (status IN (
                          'PENDING',
                          'PAID',
                          'PREPARING',
                          'OUT_FOR_DELIVERY',
                          'DELIVERED',
                          'CANCELLED'
                      )),

    subtotal          NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    delivery_fee      NUMERIC(12, 2) NOT NULL CHECK (delivery_fee >= 0),
    discount          NUMERIC(12, 2) NOT NULL CHECK (discount >= 0),
    total_amount      NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),

    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id                INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id          INTEGER NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    menu_item_id      INTEGER NOT NULL REFERENCES menu_items (id),

    item_name         VARCHAR(150) NOT NULL,
    unit_price        NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    quantity          INTEGER NOT NULL CHECK (quantity > 0)
);