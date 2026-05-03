SET
search_path = project, pg_catalog;

INSERT INTO reviews (id, rating, comment, user_id, restaurant_id)
VALUES
    (gen_random_uuid(), 5, 'Mâncare excelentă și atmosferă autentică!',
     (SELECT id FROM users WHERE username = 'andrei.popescu' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1)),

    (gen_random_uuid(), 4, 'Foarte bun, dar porțiile cam mici.',
     (SELECT id FROM users WHERE username = 'elena.ionescu' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1)),

    (gen_random_uuid(), 3, 'Serviciul a fost cam lent, dar mâncarea bună.',
     (SELECT id FROM users WHERE username = 'florin.radu' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1)),

    (gen_random_uuid(), 5, 'Cea mai bună tochitură moldovenească!',
     (SELECT id FROM users WHERE username = 'marius.stan' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1)),

    (gen_random_uuid(), 4, 'Deserturile sunt senzaționale!',
     (SELECT id FROM users WHERE username = 'georgiana.ilie' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1)),

    (gen_random_uuid(), 2, 'Mâncarea bună, dar prea scump pentru ce oferă.',
     (SELECT id FROM users WHERE username = 'doru.pavel' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1)),

    (gen_random_uuid(), 5, 'Tocănița de cartofi ca la mama acasă!',
     (SELECT id FROM users WHERE username = 'robert.mocanu' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1)),

    (gen_random_uuid(), 3, 'Meniul destul de limitat, dar bun.',
     (SELECT id FROM users WHERE username = 'ana.marinescu' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1)),

    (gen_random_uuid(), 4, 'Prețuri corecte și mâncare tradițională gustoasă.',
     (SELECT id FROM users WHERE username = 'cristina.neagu' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1));
