SET
search_path = project, pg_catalog;

CREATE TABLE reviews
(
    id           UUID PRIMARY KEY,
    rating       INT CHECK (Rating BETWEEN 1 AND 5),
    comment      TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id       UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
);