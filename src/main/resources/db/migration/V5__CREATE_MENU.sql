SET
search_path = project, pg_catalog;

CREATE TABLE menus
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    restaurant_id UUID         NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
);

CREATE TABLE menu_items
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    description TEXT,
    menu_id     UUID            NOT NULL,
    FOREIGN KEY (menu_id) REFERENCES menus (id) ON DELETE CASCADE
);