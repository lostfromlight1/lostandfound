
UPDATE posts SET location = 'YANGON' WHERE location IS NULL OR location = '';


ALTER TABLE posts
ALTER COLUMN location TYPE VARCHAR(255) USING location::varchar;