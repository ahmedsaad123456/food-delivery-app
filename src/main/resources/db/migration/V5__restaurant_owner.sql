-- Day 5: a restaurant belongs to a user (RESTAURANT_OWNER).
-- Needed later for ownership checks (a user may only edit his own restaurant).
-- Nullable so that restaurants created before this migration remain valid.
ALTER TABLE restaurants
    ADD COLUMN owner_id INTEGER REFERENCES users (id) ON DELETE SET NULL;
