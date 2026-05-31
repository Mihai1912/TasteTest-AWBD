-- Flyway migration to ensure restaurant-related tables exist
CREATE SCHEMA IF NOT EXISTS project;

CREATE TABLE IF NOT EXISTS project.restaurants (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255),
  address TEXT
);

CREATE TABLE IF NOT EXISTS project.reviews (
  id BIGSERIAL PRIMARY KEY,
  content TEXT,
  user_id BIGINT,
  restaurant_id BIGINT,
  rating INT
);
