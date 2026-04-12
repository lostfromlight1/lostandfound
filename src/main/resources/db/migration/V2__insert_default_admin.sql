-- ==========================================
-- Insert Default Admin Account
-- ==========================================

INSERT INTO users (
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
VALUES (
           'admin@lostandfound.com',
           -- This is the BCrypt hash for the password: 'Admin@1234!'
           '$2a$10$1nTn1nYh1C5H0Km79CJaOOkq9depvKs27ekglnc.JTjVc9Uhq7CAi',
           'LOCAL',
           'ADMIN',
           FALSE,
           TRUE,            -- <== Sets email_verified to TRUE immediately
           'System Admin',
           TRUE,
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       )
-- If the admin already exists (e.g., from a previous run), do nothing instead of crashing
    ON CONFLICT (email) DO NOTHING;