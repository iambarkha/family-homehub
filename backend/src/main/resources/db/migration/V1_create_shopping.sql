CREATE TABLE shopping_items (
                                id UUID PRIMARY KEY,
                                name VARCHAR(255) NOT NULL,
                                quantity VARCHAR(100),
                                category VARCHAR(30) NOT NULL,
                                purchased BOOLEAN NOT NULL DEFAULT FALSE,
                                ai_suggested BOOLEAN NOT NULL DEFAULT FALSE,
                                added_by_id UUID REFERENCES users(id),
                                family_group_id UUID NOT NULL REFERENCES family_groups(id),
                                created_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_shopping_family_category
    ON shopping_items(family_group_id, category);