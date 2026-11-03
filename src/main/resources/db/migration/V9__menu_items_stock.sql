-- Day 11: inventory. Stock lives on the menu item.
ALTER TABLE menu_items
    ADD COLUMN stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0);
