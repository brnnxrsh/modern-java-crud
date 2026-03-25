CREATE TYPE PROJECT_STATUS AS ENUM (
	'IN_REVIEW',
	'REVIEW_COMPLETED',
	'REVIEW_APPROVED',
	'STARTED',
	'PLANNED',
	'IN_PROGRESS',
	'FINISHED',
	'CANCELLED'
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    start_at DATE NOT NULL,
    end_at DATE,
    expected_end_at DATE NOT NULL,
    total_budget NUMERIC(15,2) NOT NULL,
    status PROJECT_STATUS NOT NULL DEFAULT 'IN_REVIEW',
    description VARCHAR(500),
    member_id BIGINT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_projects_expected_end_at CHECK (expected_end_at >= start_at),
    CONSTRAINT chk_projects_end_at CHECK (end_at IS NULL OR end_at >= start_at)
);

CREATE INDEX idx_projects_start_at
ON projects (start_at);

CREATE INDEX idx_projects_total_budget
ON projects (total_budget);

CREATE INDEX idx_projects_status
ON projects (status);

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_projects_name
ON projects USING gin (name gin_trgm_ops);