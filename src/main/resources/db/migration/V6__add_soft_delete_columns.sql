ALTER TABLE app_users ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE financial_records ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_app_users_deleted ON app_users(deleted);
CREATE INDEX IF NOT EXISTS idx_financial_records_deleted ON financial_records(deleted);
