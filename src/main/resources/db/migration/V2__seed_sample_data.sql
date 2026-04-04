INSERT INTO app_users (name, username, email, password, role, status)
SELECT 'Admin User', 'admin', 'admin@finance.local', '{noop}password', 'ADMIN', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

INSERT INTO app_users (name, username, email, password, role, status)
SELECT 'Analyst User', 'analyst', 'analyst@finance.local', '{noop}password', 'ANALYST', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'analyst');

INSERT INTO app_users (name, username, email, password, role, status)
SELECT 'Viewer User', 'viewer', 'viewer@finance.local', '{noop}password', 'VIEWER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'viewer');

INSERT INTO app_users (name, username, email, password, role, status)
SELECT 'Dormant User', 'inactive', 'inactive@finance.local', '{noop}password', 'ANALYST', 'INACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'inactive');

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 7500.00, 'INCOME', 'Salary', DATE '2026-04-01', 'Monthly salary credit', u.id
FROM app_users u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 7500.00
        AND fr.type = 'INCOME'
        AND fr.category = 'Salary'
        AND fr.date = DATE '2026-04-01'
  );

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 420.50, 'EXPENSE', 'Groceries', DATE '2026-04-02', 'Weekly groceries', u.id
FROM app_users u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 420.50
        AND fr.type = 'EXPENSE'
        AND fr.category = 'Groceries'
        AND fr.date = DATE '2026-04-02'
  );

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 1800.00, 'EXPENSE', 'Rent', DATE '2026-04-03', 'April rent', u.id
FROM app_users u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 1800.00
        AND fr.type = 'EXPENSE'
        AND fr.category = 'Rent'
        AND fr.date = DATE '2026-04-03'
  );

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 250.00, 'INCOME', 'Freelance', DATE '2026-03-28', 'Dashboard consulting', u.id
FROM app_users u
WHERE u.username = 'analyst'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 250.00
        AND fr.type = 'INCOME'
        AND fr.category = 'Freelance'
        AND fr.date = DATE '2026-03-28'
  );

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 89.99, 'EXPENSE', 'Software', DATE '2026-03-26', 'Productivity subscription', u.id
FROM app_users u
WHERE u.username = 'analyst'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 89.99
        AND fr.type = 'EXPENSE'
        AND fr.category = 'Software'
        AND fr.date = DATE '2026-03-26'
  );

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 120.00, 'EXPENSE', 'Utilities', DATE '2026-02-15', 'Electricity bill', u.id
FROM app_users u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 120.00
        AND fr.type = 'EXPENSE'
        AND fr.category = 'Utilities'
        AND fr.date = DATE '2026-02-15'
  );

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 300.00, 'INCOME', 'Investments', DATE '2026-02-10', 'Dividend payout', u.id
FROM app_users u
WHERE u.username = 'analyst'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 300.00
        AND fr.type = 'INCOME'
        AND fr.category = 'Investments'
        AND fr.date = DATE '2026-02-10'
  );

INSERT INTO financial_records (amount, type, category, date, notes, created_by_user_id)
SELECT 65.75, 'EXPENSE', 'Transport', DATE '2026-01-14', 'Fuel refill', u.id
FROM app_users u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_records fr
      WHERE fr.created_by_user_id = u.id
        AND fr.amount = 65.75
        AND fr.type = 'EXPENSE'
        AND fr.category = 'Transport'
        AND fr.date = DATE '2026-01-14'
  );
