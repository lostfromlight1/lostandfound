-- 1. Create Category first (Post depends on this)
CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
    -- From BaseEntity
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          deleted_at TIMESTAMP
);

-- 2. Create Posts
CREATE TABLE posts (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(255),
                       description VARCHAR(2000),
                       post_type VARCHAR(50),   -- Enum as String
                       status VARCHAR(50),      -- Enum as String
                       location VARCHAR(255),
                       lost_found_date DATE,
                       contact_info VARCHAR(255),


    -- Foreign Keys
                       user_id BIGINT,
                       category_id BIGINT,

    -- From BaseEntity (Inherited)
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       deleted_at TIMESTAMP,

    -- Constraints
                       CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id),
                       CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES category(id)
);