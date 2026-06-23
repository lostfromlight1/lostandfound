CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  recipient_id BIGINT NOT NULL,
  type VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
  title VARCHAR(255) NOT NULL,
  message TEXT,
  post_id BIGINT,
  comment_id BIGINT,
  reply_id BIGINT,
  fcm_token VARCHAR(500),
  push_sent BOOLEAN DEFAULT false,
  read_at TIMESTAMP,
  -- Audit fields
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT true,
  deleted_at TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_notifications_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE SET NULL,
  CONSTRAINT fk_notifications_comment FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE SET NULL,
  CONSTRAINT fk_notifications_reply FOREIGN KEY (reply_id) REFERENCES replies (id) ON DELETE SET NULL
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);

CREATE INDEX idx_notifications_recipient_id ON notifications (recipient_id);

CREATE INDEX idx_notifications_status ON notifications (status);

CREATE INDEX idx_notifications_type ON notifications (type);

CREATE INDEX idx_notifications_recipient_status ON notifications (recipient_id, status);

CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);

CREATE INDEX idx_notifications_post_id ON notifications (post_id);

CREATE INDEX idx_notifications_comment_id ON notifications (comment_id);

CREATE INDEX idx_notifications_reply_id ON notifications (reply_id);

CREATE INDEX idx_notifications_active ON notifications (is_active);
