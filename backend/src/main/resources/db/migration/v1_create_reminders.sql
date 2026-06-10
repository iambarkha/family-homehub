CREATE TABLE reminders (
                           id UUID PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           message TEXT,
                           trigger_at TIMESTAMP NOT NULL,
                           sent BOOLEAN NOT NULL DEFAULT FALSE,
                           recurrence_pattern VARCHAR(20) NOT NULL DEFAULT 'NONE',
                           assigned_to_id UUID REFERENCES users(id),
                           family_group_id UUID NOT NULL REFERENCES family_groups(id),
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_reminder_unsent
    ON reminders(sent, trigger_at)
    WHERE sent = FALSE;