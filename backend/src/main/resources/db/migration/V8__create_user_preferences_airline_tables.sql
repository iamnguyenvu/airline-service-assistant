-- Create ElementCollection tables for UserPreferences

CREATE TABLE IF NOT EXISTS user_preferences_preferred_airlines (
    user_id UUID NOT NULL,
    airline VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_preferences_preferred_airlines
        FOREIGN KEY (user_id)
        REFERENCES user_preferences(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_preferences_preferred_airlines_user_id
    ON user_preferences_preferred_airlines(user_id);

CREATE TABLE IF NOT EXISTS user_preferences_avoid_airlines (
    user_id UUID NOT NULL,
    airline VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_preferences_avoid_airlines
        FOREIGN KEY (user_id)
        REFERENCES user_preferences(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_preferences_avoid_airlines_user_id
    ON user_preferences_avoid_airlines(user_id);
