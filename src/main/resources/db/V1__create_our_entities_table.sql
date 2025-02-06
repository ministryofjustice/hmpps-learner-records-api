-- This is used by flyway to create the table if it's not there. I think flyway can configure other things.

CREATE TABLE IF NOT EXISTS our_entities
(
    id VARCHAR(36) UNIQUE,
    something VARCHAR(255) NOT NULL UNIQUE,
    somethingElse VARCHAR(255) NOT NULL UNIQUE
);