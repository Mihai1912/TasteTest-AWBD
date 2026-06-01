-- Flyway migration to ensure auth/user related tables exist
CREATE SCHEMA IF NOT EXISTS project;

SET search_path = project, pg_catalog;

CREATE SEQUENCE IF NOT EXISTS roles_seq START WITH 4 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS project.roles (
  id INTEGER PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS project.users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS project.user_role (
  user_id UUID NOT NULL REFERENCES project.users(id) ON DELETE CASCADE,
  role_id INTEGER NOT NULL REFERENCES project.roles(id),
  PRIMARY KEY (user_id, role_id)
);

-- Insert default roles if they don't exist
INSERT INTO project.roles (id, name) VALUES 
  (1, 'ADMIN'),
  (2, 'USER'),
  (3, 'RESTAURANT_OWNER')
ON CONFLICT (id) DO NOTHING;
