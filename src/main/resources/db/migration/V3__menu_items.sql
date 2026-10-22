-- menu items.
-- Money is NUMERIC(12,2), never FLOAT/DOUBLE. It maps to BigDecimal.
CREATE TABLE menu_items (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    restaurant_id BIGINT         NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    category_id   BIGINT         NOT NULL REFERENCES categories (id),
    name          VARCHAR(150)   NOT NULL,
    description   TEXT,
    price         NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    available     BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, name)
);
