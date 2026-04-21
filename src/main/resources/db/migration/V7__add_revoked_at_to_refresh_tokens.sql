-- Migration to add revoked_at column to refresh_tokens table
ALTER TABLE refresh_tokens ADD COLUMN revoked_at TIMESTAMP WITH TIME ZONE;