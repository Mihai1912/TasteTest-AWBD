-- Flyway migration to ensure auth/user related tables exist
CREATE SCHEMA IF NOT EXISTS project;

CREATE TABLE IF NOT EXISTS project.roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS project.users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE,
  username VARCHAR(255),
  password VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS project.user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
);
