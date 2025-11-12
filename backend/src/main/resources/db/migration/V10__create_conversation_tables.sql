-- Create conversation table
CREATE TABLE conversation (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    title VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(session_id)
);

CREATE INDEX idx_conversation_session ON conversation(session_id);
CREATE INDEX idx_conversation_updated ON conversation(updated_at DESC);

-- Create conversation_message table
CREATE TABLE conversation_message (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'user' or 'assistant'
    content TEXT NOT NULL,
    used_tools BOOLEAN DEFAULT FALSE,
    model VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_conversation 
        FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE,
    CONSTRAINT chk_role_valid CHECK (role IN ('user', 'assistant'))
);

CREATE INDEX idx_message_conversation ON conversation_message(conversation_id, created_at);
CREATE INDEX idx_message_created ON conversation_message(created_at DESC);

