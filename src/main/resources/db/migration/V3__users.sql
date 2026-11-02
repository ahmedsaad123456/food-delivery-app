-- Day 4: users for authentication.
-- "role" is a temporary column. Day 5 replaces it with roles + permissions tables.
CREATE TABLE users (
    id             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    full_name      VARCHAR(150) NOT NULL,
    phone          VARCHAR(30),
    role           VARCHAR(30)  NOT NULL DEFAULT 'CUSTOMER'
                   CHECK (role IN ('CUSTOMER', 'RESTAURANT_OWNER', 'DRIVER', 'ADMIN')),
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
