SET
search_path = project, pg_catalog;

CREATE TABLE feedback
(
    id          UUID PRIMARY KEY,
    feedback_type text NOT NULL,
    experience   text NOT NULL,
    comment      text NOT NULL
);