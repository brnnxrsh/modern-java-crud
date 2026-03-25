CREATE INDEX idx_projects_duration_months 
ON projects (fn_months_between(start_at, expected_end_at));

CREATE INDEX idx_projects_effective_end_at 
ON projects (fn_effective_date(end_at, expected_end_at));
