-- ==========================================
-- 1. COMMENTS TABLE (Updated with BaseEntity & Images)
-- ==========================================
CREATE TABLE comments (
  id BIGSERIAL PRIMARY KEY,
  content TEXT NOT NULL,
  -- NEW: Comment Image Fields
  image_url VARCHAR(1000),
  image_public_id VARCHAR(255),
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  -- BaseEntity fields
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_post_id ON comments (post_id);

CREATE INDEX idx_comments_user_id ON comments (user_id);

