SET
search_path = project, pg_catalog;

CREATE TABLE restaurants
(
    id       UUID PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    address  VARCHAR(255) NOT NULL,
    phone    VARCHAR(20),
    website  VARCHAR(100),
    schedule VARCHAR(100),
    owner_id  UUID         NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);