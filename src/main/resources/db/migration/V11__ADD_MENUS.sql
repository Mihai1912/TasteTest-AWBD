SET
search_path = project, pg_catalog;

INSERT INTO menus (id, name, restaurant_id)
VALUES (gen_random_uuid(), 'Meniu Mâncare',
        (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1)),
    (gen_random_uuid(), 'Meniu Băuturi',
     (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1));

INSERT INTO menus (id, name, restaurant_id)
VALUES (gen_random_uuid(), 'Meniu Mâncare',
        (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1)),
    (gen_random_uuid(), 'Meniu Băuturi',
     (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1));

INSERT INTO menus (id, name, restaurant_id)
VALUES (gen_random_uuid(), 'Meniu Mâncare',
        (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1)),
    (gen_random_uuid(), 'Meniu Băuturi',
     (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1));

INSERT INTO menu_items (id, name, price, description, menu_id)
VALUES (gen_random_uuid(), 'Ciorbă de burtă', 25.00, 'Ciorbă autentică servită cu smântână și ardei iute.',
        (SELECT id
         FROM menus
         WHERE name = 'Meniu Mâncare'
           AND restaurant_id =
               (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1) LIMIT 1)),

    (gen_random_uuid(), 'Sarmale cu mămăligă', 30.00, 'Sarmale gătite lent, servite cu mămăligă și smântână.',
     (
SELECT id
FROM menus
WHERE name = 'Meniu Mâncare'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1) LIMIT 1))
    , (gen_random_uuid()
    , 'Papanasi'
    , 18.00
    , 'Papanași pufoși cu dulceață de afine și smântână.'
    , (
SELECT id
FROM menus
WHERE name = 'Meniu Mâncare'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1) LIMIT 1));

INSERT INTO menu_items (id, name, price, description, menu_id)
VALUES (gen_random_uuid(), 'Tochitură moldovenească', 35.00,
        'Carne de porc, cârnați, brânză și ou, servite cu mămăligă.',
        (SELECT id
         FROM menus
         WHERE name = 'Meniu Mâncare'
           AND restaurant_id =
               (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1) LIMIT 1)),

    (gen_random_uuid(), 'Plăcinte poale-n brâu', 15.00, 'Plăcinte tradiționale cu brânză dulce și stafide.',
     (
SELECT id
FROM menus
WHERE name = 'Meniu Mâncare'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1) LIMIT 1))
    , (gen_random_uuid()
    , 'Mămăligă cu brânză și smântână'
    , 20.00
    , 'Mâncare simplă și gustoasă, exact ca la bunica acasă.'
    , (
SELECT id
FROM menus
WHERE name = 'Meniu Mâncare'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1) LIMIT 1));

INSERT INTO menu_items (id, name, price, description, menu_id)
VALUES (gen_random_uuid(), 'Supă de găină cu tăiței', 22.00, 'Supă gustoasă cu tăiței de casă, specifică tradiției.',
        (SELECT id
         FROM menus
         WHERE name = 'Meniu Mâncare'
           AND restaurant_id =
               (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1) LIMIT 1)),

    (gen_random_uuid(), 'Mămăligă cu brânză și smântână', 18.00, 'O combinație perfectă de mămăligă caldă, brânză și smântână.',
     (
SELECT id
FROM menus
WHERE name = 'Meniu Mâncare'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1) LIMIT 1))
    , (gen_random_uuid()
    , 'Ruladă de porc cu varză'
    , 28.00
    , 'Ruladă suculentă de porc servită cu varză călită.'
    , (
SELECT id
FROM menus
WHERE name = 'Meniu Mâncare'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1) LIMIT 1));

INSERT INTO menu_items (id, name, price, description, menu_id)
VALUES (gen_random_uuid(), 'Vin Fetească Neagră', 15.00, 'Vin roșu sec, cu note de fructe de pădure.',
        (SELECT id
         FROM menus
         WHERE name = 'Meniu Băuturi'
           AND restaurant_id =
               (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1) LIMIT 1)),

    (gen_random_uuid(), 'Bere Artizanală', 10.00, 'Bere nefiltrată, făcută după rețetă tradițională.',
     (
SELECT id
FROM menus
WHERE name = 'Meniu Băuturi'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1) LIMIT 1))
    , (gen_random_uuid()
    , 'Limonadă de casă'
    , 7.00
    , 'Limonadă naturală cu mentă proaspătă.'
    , (
SELECT id
FROM menus
WHERE name = 'Meniu Băuturi'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1) LIMIT 1));

INSERT INTO menu_items (id, name, price, description, menu_id)
VALUES (gen_random_uuid(), 'Vinulet de casă', 12.00, 'Viu și aromat, perfect pentru o masă tradițională.',
        (SELECT id
         FROM menus
         WHERE name = 'Meniu Băuturi'
           AND restaurant_id =
               (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1) LIMIT 1)),

    (gen_random_uuid(), 'Țuică de prună', 18.00, 'Țuică artizanală de prună, din propria producție.',
     (
SELECT id
FROM menus
WHERE name = 'Meniu Băuturi'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1) LIMIT 1))
    , (gen_random_uuid()
    , 'Suc natural de mere'
    , 6.00
    , 'Suc natural proaspăt din mere de țară.'
    , (
SELECT id
FROM menus
WHERE name = 'Meniu Băuturi'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1) LIMIT 1));

INSERT INTO menu_items (id, name, price, description, menu_id)
VALUES (gen_random_uuid(), 'Vin alb demidulce', 14.00, 'Vin alb cu gust dulceag, perfect pentru mesele de vară.',
        (SELECT id
         FROM menus
         WHERE name = 'Meniu Băuturi'
           AND restaurant_id =
               (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1) LIMIT 1)),

    (gen_random_uuid(), 'Cozonac cu rom', 8.00, 'Băutură caldă pe bază de rom și cozonac, ideală pentru serile reci.',
     (
SELECT id
FROM menus
WHERE name = 'Meniu Băuturi'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1) LIMIT 1))
    , (gen_random_uuid()
    , 'Apă plată'
    , 4.00
    , 'Apă plată de izvor, răcoritoare și pură.'
    , (
SELECT id
FROM menus
WHERE name = 'Meniu Băuturi'
  AND restaurant_id =
    (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1) LIMIT 1));
