-- Lab table: NO CHECK (stock >= 0) on purpose, so we can SEE the negative stock.
CREATE TABLE inventory_lab (
    id      INT    PRIMARY KEY,
    stock   INT    NOT NULL,
    version BIGINT NOT NULL DEFAULT 0 -- optimistic locking version number ( use it to detect concurrent updates ) like when you want to update a row, you check if the version is the same as when you read it, if not, someone else updated it and you can handle that case
);