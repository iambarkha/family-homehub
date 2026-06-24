CREATE TABLE nutrition_logs (
                                id UUID PRIMARY KEY,
                                log_date DATE NOT NULL,
                                total_calories INT,
                                total_protein_grams INT,
                                total_carbs_grams INT,
                                total_fat_grams INT,
                                alert_sent BOOLEAN NOT NULL DEFAULT FALSE,
                                family_group_id UUID NOT NULL REFERENCES family_groups(id),
                                created_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP NOT NULL,
                                UNIQUE (family_group_id, log_date)
);