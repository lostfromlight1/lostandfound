-- ==========================================
-- Insert Default Admin Account
-- ==========================================
INSERT INTO
  users (
    email,
    password,
    provider,
    role,
    is_locked,
    email_verified,
    display_name,
    is_active,
    created_at,
    updated_at
  )
VALUES
  (
    'admin@lostandfound.com',
    '$2a$10$1nTn1nYh1C5H0Km79CJaOOkq9depvKs27ekglnc.JTjVc9Uhq7CAi', -- Hash for 'Admin@1234!'
    'LOCAL',
    'ADMIN',
    FALSE,
    TRUE,
    'System Admin',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
  ) ON CONFLICT (email) DO NOTHING;
