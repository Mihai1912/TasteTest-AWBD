-- Repair migration for databases where the reviews table was first created by
-- the old restaurant-service V16 (column "content") instead of the canonical
-- "comment" column used by the Review entity. Without this, getTopRatedRestaurants()
-- fails with "column comment does not exist" and the /restaurant/top-rated
-- endpoint returns 400. Idempotent: safe whether the table already has the
-- correct schema (e.g. created by the monolith) or the broken one.
SET search_path = project, pg_catalog;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema = 'project' AND table_name = 'reviews' AND column_name = 'content')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema = 'project' AND table_name = 'reviews' AND column_name = 'comment') THEN
    ALTER TABLE project.reviews RENAME COLUMN content TO comment;
  END IF;
END $$;

ALTER TABLE project.reviews ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
