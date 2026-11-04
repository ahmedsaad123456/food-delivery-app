-- Null means unlimited stock, so we need to allow null values in the stock column of the menu_items table.
ALTER TABLE menu_items
    ALTER COLUMN stock DROP NOT NULL;