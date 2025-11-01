-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create flight_snapshot table
CREATE TABLE flight_snapshot (
    id BIGSERIAL PRIMARY KEY,
    snapshot_date DATE NOT NULL,
    dep_iata VARCHAR(3) NOT NULL,
    arr_iata VARCHAR(3) NOT NULL,
    dep_time TIMESTAMP NOT NULL,
    arr_time TIMESTAMP NOT NULL,
    carrier VARCHAR(3) NOT NULL,
    flight_no VARCHAR(10) NOT NULL,
    duration_min INTEGER NOT NULL,
    stops SMALLINT DEFAULT 0,
    fare_family VARCHAR(50),
    baggage_kg DECIMAL(5,2),
    price_cents BIGINT NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    source VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_price_positive CHECK (price_cents > 0),
    CONSTRAINT chk_duration_positive CHECK (duration_min > 0)
);

-- Create indexes for flight_snapshot
CREATE INDEX idx_flight_snapshot_route_date 
    ON flight_snapshot(dep_iata, arr_iata, snapshot_date);
CREATE INDEX idx_flight_snapshot_dep_time 
    ON flight_snapshot(dep_iata, arr_iata, dep_time);
CREATE INDEX idx_flight_snapshot_created 
    ON flight_snapshot(created_at);

-- Create flight_raw table (for detailed payload, TTL 90 days)
CREATE TABLE flight_raw (
    id BIGSERIAL PRIMARY KEY,
    snapshot_id BIGINT NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flight_raw_snapshot 
        FOREIGN KEY (snapshot_id) REFERENCES flight_snapshot(id) ON DELETE CASCADE
);

CREATE INDEX idx_flight_raw_snapshot ON flight_raw(snapshot_id);
CREATE INDEX idx_flight_raw_created ON flight_raw(created_at);

-- Create route_stats_daily table
CREATE TABLE route_stats_daily (
    id BIGSERIAL PRIMARY KEY,
    route_key VARCHAR(10) NOT NULL, -- e.g., 'SGN-HAN'
    date DATE NOT NULL,
    min_price_cents BIGINT,
    p50_price_cents BIGINT,
    p90_price_cents BIGINT,
    avg_duration_min INTEGER,
    avg_co2_kg DECIMAL(10,2),
    flight_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(route_key, date)
);

CREATE INDEX idx_route_stats_key_date ON route_stats_daily(route_key, date);

-- Create service_docs table
CREATE TABLE service_docs (
    id BIGSERIAL PRIMARY KEY,
    source_url TEXT,
    airline_code VARCHAR(3),
    doc_type VARCHAR(50), -- 'policy', 'fare_rule', 'baggage', etc.
    version_tag VARCHAR(100),
    raw_text TEXT, -- Optional, prefer storage_url
    storage_url TEXT, -- URL to Supabase Storage
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_docs_airline ON service_docs(airline_code, doc_type);

-- Create service_doc_chunks table (for RAG)
CREATE TABLE service_doc_chunks (
    id BIGSERIAL PRIMARY KEY,
    doc_id BIGINT NOT NULL,
    section_title VARCHAR(500),
    chunk TEXT NOT NULL,
    embedding vector(384), -- For all-MiniLM-L6-v2
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chunk_doc 
        FOREIGN KEY (doc_id) REFERENCES service_docs(id) ON DELETE CASCADE
);

CREATE INDEX idx_chunk_doc ON service_doc_chunks(doc_id);
-- HNSW index for vector similarity search
CREATE INDEX idx_chunk_embedding ON service_doc_chunks 
    USING hnsw (embedding vector_cosine_ops);

-- Create fare_rule_norm table
CREATE TABLE fare_rule_norm (
    id BIGSERIAL PRIMARY KEY,
    airline_code VARCHAR(3) NOT NULL,
    cabin VARCHAR(20), -- 'economy', 'business', 'first'
    fare_family VARCHAR(50),
    change_fee_cents BIGINT,
    refund_fee_cents BIGINT,
    refund_allowed BOOLEAN DEFAULT false,
    change_deadline_hours INTEGER, -- Hours before departure
    no_show_fee_cents BIGINT,
    carry_on_kg DECIMAL(5,2),
    checked_bag_kg DECIMAL(5,2),
    effective_from DATE,
    effective_to DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fare_rule_airline ON fare_rule_norm(airline_code, cabin, fare_family);

-- Create bookings_mock table
CREATE TABLE bookings_mock (
    id BIGSERIAL PRIMARY KEY,
    pnr VARCHAR(6) UNIQUE NOT NULL,
    user_id UUID NOT NULL, -- From Supabase Auth
    flight_snapshot_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'confirmed', -- 'held', 'confirmed', 'cancelled'
    pdf_url TEXT, -- Signed URL to Supabase Storage
    payload JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP, -- For held bookings
    CONSTRAINT fk_booking_snapshot 
        FOREIGN KEY (flight_snapshot_id) REFERENCES flight_snapshot(id)
);

CREATE INDEX idx_bookings_user ON bookings_mock(user_id);
CREATE INDEX idx_bookings_pnr ON bookings_mock(pnr);

-- Create users_public table (synced from Supabase auth.users)
CREATE TABLE users_public (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255),
    preferred_airline VARCHAR(3),
    preferred_cabin VARCHAR(20),
    max_budget_cents BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user preferences table
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    preferred_seat VARCHAR(10), -- 'window', 'aisle', 'any'
    max_stops SMALLINT DEFAULT 1,
    preferred_airlines TEXT[], -- Array of airline codes
    avoid_airlines TEXT[],
    max_duration_hours INTEGER,
    preferences JSONB, -- Flexible additional preferences
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prefs_user 
        FOREIGN KEY (user_id) REFERENCES users_public(id) ON DELETE CASCADE
);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_users_public_updated_at 
    BEFORE UPDATE ON users_public 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_preferences_updated_at 
    BEFORE UPDATE ON user_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
