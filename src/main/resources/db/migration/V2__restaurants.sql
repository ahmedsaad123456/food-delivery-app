-- Day 2: categories and restaurants.
CREATE TABLE categories (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE restaurants (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    description  TEXT,
    category_id  BIGINT       NOT NULL REFERENCES categories (id),
    address      VARCHAR(255) NOT NULL,
    phone        VARCHAR(30),
    open         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- A few starter categories so the API has something to return.
INSERT INTO categories (name) VALUES
    ('Pizza'), ('Burgers'), ('Sushi'), ('Desserts'), ('Drinks');
