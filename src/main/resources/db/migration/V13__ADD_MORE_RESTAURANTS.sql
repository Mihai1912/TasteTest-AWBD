SET
search_path = project, pg_catalog;

INSERT INTO restaurants (id, name, address, phone, website, schedule, owner_id)
VALUES
    (gen_random_uuid(), 'Hanul Berarilor', 'Str. Poenaru Bordea 2, București', '0721-555-111', 'www.hanulberarilor.ro', 'L-D: 11:00-24:00',
     (SELECT id FROM users WHERE username = 'marius.stan' LIMIT 1)),

    (gen_random_uuid(), 'Trattoria Buongiorno', 'Calea Victoriei 120, București', '0722-555-222', 'www.buongiorno.ro', 'L-V: 12:00-23:00, S-D: 12:00-24:00',
     (SELECT id FROM users WHERE username = 'marius.stan' LIMIT 1)),

    (gen_random_uuid(), 'Sushi Master', 'Str. Dorobanților 14, București', '0723-555-333', 'www.sushimaster.ro', 'L-D: 12:00-23:00',
     (SELECT id FROM users WHERE username = 'marius.stan' LIMIT 1)),

    (gen_random_uuid(), 'Pizzeria Napoli', 'Bld. Magheru 18, București', '0724-555-444', 'www.pizzerianapoli.ro', 'L-D: 10:00-23:00',
     (SELECT id FROM users WHERE username = 'marius.stan' LIMIT 1)),

    (gen_random_uuid(), 'Bistro Parizian', 'Str. Franceză 32, Cluj-Napoca', '0741-555-555', 'www.bistroparizian.ro', 'L-V: 09:00-22:00, S-D: 10:00-23:00',
     (SELECT id FROM users WHERE username = 'ionut.dumitru' LIMIT 1)),

    (gen_random_uuid(), 'Carnivor Steak House', 'Str. Memorandumului 5, Cluj-Napoca', '0742-555-666', 'www.carnivor.ro', 'L-D: 12:00-24:00',
     (SELECT id FROM users WHERE username = 'ionut.dumitru' LIMIT 1)),

    (gen_random_uuid(), 'Gradina Verde', 'Str. Avram Iancu 12, Cluj-Napoca', '0743-555-777', 'www.gradinaverde.ro', 'L-D: 09:00-22:00',
     (SELECT id FROM users WHERE username = 'ionut.dumitru' LIMIT 1)),

    (gen_random_uuid(), 'Taverna Greaca', 'Piața Unirii 7, Cluj-Napoca', '0744-555-888', 'www.tavernagreaca.ro', 'L-D: 11:00-23:00',
     (SELECT id FROM users WHERE username = 'ionut.dumitru' LIMIT 1)),

    (gen_random_uuid(), 'El Tapas Bar', 'Str. Alba Iulia 22, Timișoara', '0731-555-999', 'www.eltapas.ro', 'L-V: 17:00-01:00, S-D: 12:00-02:00',
     (SELECT id FROM users WHERE username = 'doru.pavel' LIMIT 1)),

    (gen_random_uuid(), 'Pho Saigon', 'Str. Eugeniu de Savoya 9, Timișoara', '0732-556-000', 'www.phosaigon.ro', 'L-D: 11:30-22:30',
     (SELECT id FROM users WHERE username = 'doru.pavel' LIMIT 1)),

    (gen_random_uuid(), 'Burger Republic', 'Bld. Revoluției 15, Timișoara', '0734-556-111', 'www.burgerrepublic.ro', 'L-D: 10:00-23:00',
     (SELECT id FROM users WHERE username = 'doru.pavel' LIMIT 1)),

    (gen_random_uuid(), 'Vegan Garden', 'Str. Lucian Blaga 4, Timișoara', '0735-556-222', 'www.vegangarden.ro', 'L-S: 10:00-22:00',
     (SELECT id FROM users WHERE username = 'doru.pavel' LIMIT 1)),

    (gen_random_uuid(), 'Cofetăria Capșa', 'Calea Victoriei 36, București', '0725-556-333', 'www.capsa.ro', 'L-D: 08:00-22:00',
     (SELECT id FROM users WHERE username = 'marius.stan' LIMIT 1)),

    (gen_random_uuid(), 'Casa Pescarului', 'Str. Dunării 50, Constanța', '0241-556-444', 'www.casapescarului.ro', 'L-D: 11:00-23:00',
     (SELECT id FROM users WHERE username = 'ionut.dumitru' LIMIT 1)),

    (gen_random_uuid(), 'Crama Domnească', 'Str. Bisericii 7, Sibiu', '0269-556-555', 'www.cramadomneasca.ro', 'M-D: 12:00-23:00',
     (SELECT id FROM users WHERE username = 'doru.pavel' LIMIT 1));
