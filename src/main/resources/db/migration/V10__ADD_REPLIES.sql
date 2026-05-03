SET
search_path = project, pg_catalog;

INSERT INTO replies (id, text, review_id, restaurant_id)
VALUES
    (gen_random_uuid(), 'Vă mulțumim pentru apreciere! Vă așteptăm cu drag din nou!',
     (SELECT id FROM reviews WHERE comment = 'Mâncare excelentă și atmosferă autentică!' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1)),

    (gen_random_uuid(), 'Ne pare rău pentru porțiile mici, vom ține cont de feedback!',
     (SELECT id FROM reviews WHERE comment = 'Foarte bun, dar porțiile cam mici.' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1)),

    (gen_random_uuid(), 'Încercăm să îmbunătățim serviciul. Mulțumim pentru recenzie!',
     (SELECT id FROM reviews WHERE comment = 'Serviciul a fost cam lent, dar mâncarea bună.' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Casa Românească' LIMIT 1)),

    (gen_random_uuid(), 'Ne bucurăm că v-a plăcut tochitura noastră!',
     (SELECT id FROM reviews WHERE comment = 'Cea mai bună tochitură moldovenească!' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1)),

    (gen_random_uuid(), 'Mulțumim! Deserturile sunt preparate cu ingrediente proaspete.',
     (SELECT id FROM reviews WHERE comment = 'Deserturile sunt senzaționale!' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1)),

    (gen_random_uuid(), 'Încercăm să menținem prețurile competitive. Sperăm să reveniți!',
     (SELECT id FROM reviews WHERE comment = 'Mâncarea bună, dar prea scump pentru ce oferă.' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'Gusturi Tradiționale' LIMIT 1)),

    (gen_random_uuid(), 'Ne bucurăm că v-a plăcut tocănița noastră tradițională!',
     (SELECT id FROM reviews WHERE comment = 'Tocănița de cartofi ca la mama acasă!' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1)),

    (gen_random_uuid(), 'Încercăm să extindem meniul. Vă mulțumim pentru sugestie!',
     (SELECT id FROM reviews WHERE comment = 'Meniul destul de limitat, dar bun.' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1)),

    (gen_random_uuid(), 'Vă mulțumim pentru feedback! Vă mai așteptăm!',
     (SELECT id FROM reviews WHERE comment = 'Prețuri corecte și mâncare tradițională gustoasă.' LIMIT 1),
     (SELECT id FROM restaurants WHERE name = 'La Bunica' LIMIT 1));
