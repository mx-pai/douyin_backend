CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    bio TEXT,
    gender VARCHAR(50),
    city VARCHAR(100),
    account VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255),
    cover_url VARCHAR(255),
    cover_width INT,
    cover_height INT,
    is_video BOOLEAN DEFAULT FALSE,
    media_url VARCHAR(255),
    images JSONB,
    like_count BIGINT DEFAULT 0,
    comment_count BIGINT DEFAULT 0,
    favorite_count BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    is_public BOOLEAN DEFAULT TRUE,
    status SMALLINT DEFAULT 0
);

CREATE INDEX idx_notes_feed ON notes(is_public, created_at DESC, id DESC);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    note_id BIGINT NOT NULL REFERENCES notes(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    parent_id BIGINT,
    content TEXT,
    location VARCHAR(100),
    like_count BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_comments_note ON comments(note_id, created_at DESC, id DESC);
CREATE INDEX idx_comments_parent ON comments(parent_id);

CREATE TABLE note_likes (
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_id BIGINT NOT NULL REFERENCES notes(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, target_id)
);

CREATE TABLE note_favorites (
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_id BIGINT NOT NULL REFERENCES notes(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, target_id)
);

CREATE TABLE comment_likes (
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_id BIGINT NOT NULL REFERENCES comments(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, target_id)
);

CREATE TABLE user_relations (
    user_id BIGINT NOT NULL REFERENCES users(id),
    target_id BIGINT NOT NULL REFERENCES users(id),
    relation_type SMALLINT NOT NULL, -- 0: follow, 1: block, etc.
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, target_id)
);
