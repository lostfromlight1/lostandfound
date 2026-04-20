-- 1. Rename the old column to 'city'
ALTER TABLE posts RENAME COLUMN location TO city;

-- 2. Add the new 'location_details' string column
ALTER TABLE posts ADD COLUMN location_details VARCHAR(255);