-- Day 5: RBAC. Many-to-many: users <-> roles <-> permissions.
CREATE TABLE roles (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id        BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id  BIGINT NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id  BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id  BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Seed roles and permissions.
INSERT INTO roles (name) VALUES ('ADMIN'), ('CUSTOMER'), ('RESTAURANT_OWNER'), ('DRIVER');

INSERT INTO permissions (name) VALUES
    ('RESTAURANT_MANAGE'),
    ('MENU_CREATE'),
    ('MENU_UPDATE'),
    ('MENU_DELETE'),
    ('ORDER_CREATE'),
    ('ORDER_VIEW_OWN'),
    ('ORDER_VIEW_ALL'),
    ('ORDER_CANCEL'),
    ('ORDER_UPDATE_STATUS'),
    ('DELIVERY_UPDATE'),
    ('USER_MANAGE');

-- Role -> permissions mapping.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON
       r.name = 'ADMIN'
    OR (r.name = 'CUSTOMER'         AND p.name IN ('ORDER_CREATE', 'ORDER_VIEW_OWN', 'ORDER_CANCEL'))
    OR (r.name = 'RESTAURANT_OWNER' AND p.name IN ('RESTAURANT_MANAGE', 'MENU_CREATE', 'MENU_UPDATE',
                                                   'MENU_DELETE', 'ORDER_VIEW_ALL', 'ORDER_UPDATE_STATUS'))
    OR (r.name = 'DRIVER'           AND p.name IN ('ORDER_VIEW_ALL', 'DELIVERY_UPDATE'));

-- Move existing users from the temporary users.role column to user_roles,
-- then drop the old column.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = u.role;

ALTER TABLE users DROP COLUMN role;
