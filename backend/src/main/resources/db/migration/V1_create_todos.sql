CREATE TABLE todo_items (
                            id UUID PRIMARY KEY,
                            title VARCHAR(255) NOT NULL,
                            description TEXT,
                            priority VARCHAR(20) NOT NULL,
                            scope VARCHAR(20) NOT NULL,
                            due_date DATE,
                            completed BOOLEAN NOT NULL DEFAULT FALSE,
                            assigned_to_id UUID REFERENCES users(id),
                            family_group_id UUID NOT NULL REFERENCES family_groups(id),
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL
);