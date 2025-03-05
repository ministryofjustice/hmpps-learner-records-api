
ALTER TABLE matches ADD COLUMN match_type VARCHAR(255) NOT NULL default 'NO_MATCH_SELECTED';
ALTER TABLE matches ADD COLUMN count_of_matched_ulns VARCHAR(255) NOT NULL default '0';
