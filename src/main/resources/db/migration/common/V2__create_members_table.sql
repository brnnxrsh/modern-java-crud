CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE projects_members (
    project_id BIGSERIAL NOT NULL,
    member_id BIGSERIAL NOT NULL,
    
    PRIMARY KEY (project_id, member_id),
    
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects (id),
    
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES members (id)
);

ALTER TABLE projects ADD COLUMN manager_id BIGSERIAL;
ALTER TABLE projects ADD CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES members (id);