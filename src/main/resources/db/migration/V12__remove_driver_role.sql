-- Drivers are now plain data, so the DRIVER role is no longer needed.

-- 1) Remove the role. ON DELETE CASCADE also removes its rows in role_permissions and user_roles.
DELETE FROM roles WHERE name = 'DRIVER';


-- 2) New permissions.
--    DRIVER_MANAGE: create and delete drivers (ADMIN only).
--    DRIVER_UPDATE: change a driver's status and location (ADMIN and RESTAURANT_OWNER).
--    Viewing drivers needs no permission: any logged-in user can do it.
INSERT INTO permissions (name) VALUES ('DRIVER_MANAGE'), ('DRIVER_UPDATE');

-- 3) Grant them. ADMIN got "all permissions" only at seed time, so new ones must be granted explicitly.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON
       (r.name = 'ADMIN'            AND p.name IN ('DRIVER_MANAGE', 'DRIVER_UPDATE'))
    OR (r.name = 'RESTAURANT_OWNER' AND p.name IN ('DRIVER_UPDATE', 'DELIVERY_UPDATE'));