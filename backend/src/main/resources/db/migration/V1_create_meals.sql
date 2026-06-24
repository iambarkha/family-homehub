CREATE TABLE mean_plans (
                            id UUID PRIMARY KEY,
                            date DATE NOT NULL,
                            slot VARCHAR(20) NOT NULL,
                            meal_name VARCHAR(255) NOT NULL,
                            description TEXT,
                            cuisine VARCHAR(30),
                            estimated_calories INT,
                            estimated_protein_grams INT,
                            estimated_carb_grams INT,
                            family_group_id UUID NOT NULL REFERENCES family_groups(id),
                            created_by_id UUID NOT NULL REFERENCES users(id),
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL,

);
CREATE INDEX idx_meal_family_date
 ON UNIQUE (family_group_id, date, slot);