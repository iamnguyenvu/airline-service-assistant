-- Update vector dimensions from 384 to 768 to match nomic-embed-text model
ALTER TABLE vector_store 
DROP COLUMN IF EXISTS embedding;

-- Add embedding column with correct dimensions (768)
ALTER TABLE vector_store 
ADD COLUMN embedding vector(768);

-- Recreate the index with new dimensions
DROP INDEX IF EXISTS vector_store_embedding_idx;
CREATE INDEX vector_store_embedding_idx ON vector_store 
USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);