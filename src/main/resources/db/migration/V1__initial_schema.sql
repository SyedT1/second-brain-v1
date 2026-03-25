-- Enable pgvector extension for vector similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Create entries table
CREATE TABLE entries (
    id BIGSERIAL PRIMARY KEY,

    -- Core fields
    type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    title VARCHAR(500),
    url TEXT,

    -- Metadata
    file_path TEXT,
    metadata JSONB DEFAULT '{}'::jsonb,

    -- AI-generated enrichment
    tags JSONB DEFAULT '[]'::jsonb,
    summary TEXT,
    embedding vector(1536),  -- OpenAI text-embedding-3-small dimension

    -- Full-text search
    content_tsvector tsvector,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enriched_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_entries_type ON entries(type);
CREATE INDEX idx_entries_created_at ON entries(created_at DESC);
CREATE INDEX idx_entries_tags ON entries USING GIN(tags);
CREATE INDEX idx_entries_content_tsvector ON entries USING GIN(content_tsvector);
CREATE INDEX idx_entries_embedding ON entries USING ivfflat(embedding vector_cosine_ops) WITH (lists = 100);

-- Trigger to auto-update content_tsvector on insert/update
CREATE OR REPLACE FUNCTION update_content_tsvector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.content_tsvector :=
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.content, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.summary, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER entries_content_tsvector_update
    BEFORE INSERT OR UPDATE ON entries
    FOR EACH ROW
    EXECUTE FUNCTION update_content_tsvector();

-- Trigger to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER entries_updated_at
    BEFORE UPDATE ON entries
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();
