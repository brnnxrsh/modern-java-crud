CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_projects_name
ON projects USING gin (name gin_trgm_ops);

CREATE INDEX idx_projects_duration_months 
ON projects (fn_months_between(start_at, expected_end_at));

CREATE INDEX idx_projects_effective_end_at 
ON projects (fn_effective_date(end_at, expected_end_at));
