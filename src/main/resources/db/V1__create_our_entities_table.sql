-- This is used by flyway to create the table if it's not there. I think flyway can configure other things.
-- I need more understanding of flyway, but it looks like this will configure the postgresql database when we run our service.

CREATE TABLE IF NOT EXISTS our_entities
(
    id VARCHAR(36) UNIQUE,
    something VARCHAR(255) NOT NULL UNIQUE,
    somethingElse VARCHAR(255) NOT NULL UNIQUE
);