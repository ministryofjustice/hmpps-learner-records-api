
CREATE TABLE IF NOT EXISTS matches (
    id BIGSERIAL PRIMARY KEY,
    nomis_id VARCHAR(255) NOT NULL,
    matched_uln VARCHAR(255) NOT NULL,
    given_name VARCHAR(255) NOT NULL,
    family_name VARCHAR(255) NOT NULL,
    date_of_birth VARCHAR(255),
    gender VARCHAR(255),
    date_created TIMESTAMP NOT NULL DEFAULT NOW()
);
