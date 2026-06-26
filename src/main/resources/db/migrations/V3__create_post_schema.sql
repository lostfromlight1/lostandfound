-- ==========================================
-- 1. CATEGORY TABLE
-- ==========================================
CREATE TABLE category (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- ==========================================
-- 2. POSTS TABLE (Includes Reward and Enum properties)
-- ==========================================
CREATE TABLE posts (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  post_type VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  location VARCHAR(255) NOT NULL,
  lost_found_date DATE NOT NULL,
  contact_info VARCHAR(255),
  reward NUMERIC(10, 2),
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- ==========================================
-- 3. POST IMAGES TABLE (NEW)
-- ==========================================
CREATE TABLE post_images (
  id BIGSERIAL PRIMARY KEY,
  image_url VARCHAR(1000) NOT NULL,
  public_id VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  post_id BIGINT NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT fk_post_images_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
);

