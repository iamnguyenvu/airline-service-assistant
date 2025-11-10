-- Create vector_store table for Spring AI PgVectorStore
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(384) NOT NULL
);

-- Create HNSW index for cosine similarity search
CREATE INDEX IF NOT EXISTS idx_vector_store_embedding 
ON vector_store USING hnsw (embedding vector_cosine_ops);

-- Add comment for table documentation
COMMENT ON TABLE vector_store IS 'Vector store table for Spring AI PgVectorStore integration';
COMMENT ON COLUMN vector_store.id IS 'Unique identifier for the document chunk';
COMMENT ON COLUMN vector_store.content IS 'The text content of the document chunk';
COMMENT ON COLUMN vector_store.metadata IS 'JSON metadata associated with the document chunk';
COMMENT ON COLUMN vector_store.embedding IS '384-dimensional vector embedding of the content';