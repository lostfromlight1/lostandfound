-- ==========================================
-- 1. COMMENTS TABLE
-- ==========================================
CREATE TABLE comments (
  id BIGSERIAL PRIMARY KEY,
  content TEXT NOT NULL,
  image_url VARCHAR(1000),
  image_public_id VARCHAR(255),
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_post_id ON comments (post_id);

CREATE INDEX idx_comments_user_id ON comments (user_id);

-- ==========================================
-- 2. REPLIES TABLE
-- ==========================================
CREATE TABLE replies (
  id BIGSERIAL PRIMARY KEY,
  content TEXT NOT NULL,
  image_url VARCHAR(500),
  image_public_id VARCHAR(255),
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reply_to_id BIGINT,
  active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT fk_replies_comment FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
  CONSTRAINT fk_replies_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_replies_reply_to FOREIGN KEY (reply_to_id) REFERENCES replies (id) ON DELETE SET NULL
);

CREATE INDEX idx_replies_comment_id ON replies (comment_id);

CREATE INDEX idx_replies_user_id ON replies (user_id);

CREATE INDEX idx_replies_reply_to_id ON replies (reply_to_id);

CREATE INDEX idx_replies_active ON replies (active);

CREATE INDEX idx_replies_comment_active ON replies (comment_id, active);
