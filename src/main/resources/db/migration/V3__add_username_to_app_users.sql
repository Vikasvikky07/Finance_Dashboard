ALTER TABLE app_users ADD COLUMN IF NOT EXISTS username VARCHAR(255);

UPDATE app_users
SET username = LOWER(
    CASE
        WHEN POSITION('@' IN email) > 1 THEN SUBSTRING(email FROM 1 FOR POSITION('@' IN email) - 1)
        ELSE email
    END
)
WHERE username IS NULL;

ALTER TABLE app_users ALTER COLUMN username SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_app_users_username ON app_users(username);
