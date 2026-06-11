CREATE TABLE pantry_items (
                              id UUID PRIMARY KEY,
                              name VARCHAR(200) NOT NULL,
                              current_quantity DECIMAL(10,2) NOT NULL,
                              unit VARCHAR(30) NOT NULL,
                              threshold_quantity DECIMAL(10,2) NOT NULL,
                              average_weekly_usage DECIMAL(10,2),
                              last_restocked_at DATE,
                              track_consumption BOOLEAN NOT NULL DEFAULT FALSE,
                              family_group_id UUID NOT NULL REFERENCES family_groups(id),
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_pantry_family
    ON pantry_items(family_group_id);