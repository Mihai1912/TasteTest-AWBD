SET
search_path = project, pg_catalog;

CREATE TABLE replies
(
    id            UUID PRIMARY KEY,
    text          TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    review_id     UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
);