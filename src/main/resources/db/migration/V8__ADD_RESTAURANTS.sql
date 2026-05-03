SET
search_path = project, pg_catalog;

INSERT INTO restaurants (id, name, address, phone, website, schedule, owner_id)
VALUES
    (gen_random_uuid(), 'Casa Românească', 'Str. Mihai Eminescu 45, București', '0722-123-456', 'www.casaromaneasca.ro', 'L-V: 10:00-22:00, S-D: 12:00-23:00',
     (SELECT id FROM users WHERE username = 'marius.stan' LIMIT 1)),

    (gen_random_uuid(), 'Gusturi Tradiționale', 'Bld. Unirii 10, Cluj-Napoca', '0745-678-901', 'www.gusturitraditionale.ro', 'L-D: 09:00-21:00',
     (SELECT id FROM users WHERE username = 'ionut.dumitru' LIMIT 1)),

    (gen_random_uuid(), 'La Bunica', 'Str. Libertății 88, Timișoara', '0733-222-333', 'www.labunica.ro', 'L-V: 08:00-20:00, S-D: 10:00-22:00',
     (SELECT id FROM users WHERE username = 'doru.pavel' LIMIT 1));
