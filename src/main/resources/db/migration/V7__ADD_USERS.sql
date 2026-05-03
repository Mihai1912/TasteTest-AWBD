SET
search_path = project, pg_catalog;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO users (id, username, email, password)
VALUES
    (gen_random_uuid(), 'andrei.popescu', 'andrei.popescu@gmail.com', crypt('parola123', gen_salt('bf'))),
    (gen_random_uuid(), 'elena.ionescu', 'elena.ionescu@yahoo.com', crypt('elena2024', gen_salt('bf'))),
    (gen_random_uuid(), 'marius.stan', 'marius.stan@outlook.com', crypt('mariuspass', gen_salt('bf'))),
    (gen_random_uuid(), 'georgiana.ilie', 'georgiana.ilie@gmail.com', crypt('georgiana88', gen_salt('bf'))),
    (gen_random_uuid(), 'ionut.dumitru', 'ionut.dumitru@yahoo.com', crypt('ionut456', gen_salt('bf'))),
    (gen_random_uuid(), 'robert.mocanu', 'robert.mocanu@gmail.com', crypt('robert789', gen_salt('bf'))),
    (gen_random_uuid(), 'cristina.neagu', 'cristina.neagu@outlook.com', crypt('cristina22', gen_salt('bf'))),
    (gen_random_uuid(), 'doru.pavel', 'doru.pavel@yahoo.com', crypt('doru999', gen_salt('bf'))),
    (gen_random_uuid(), 'ana.marinescu', 'ana.marinescu@gmail.com', crypt('ana000', gen_salt('bf'))),
    (gen_random_uuid(), 'florin.radu', 'florin.radu@outlook.com', crypt('florin321', gen_salt('bf')));

INSERT INTO user_role (user_id, role_id)
SELECT id, 1 FROM users WHERE username = 'andrei.popescu';  -- Admin
INSERT INTO user_role (user_id, role_id)
SELECT id, 2 FROM users WHERE username = 'elena.ionescu';  -- User
INSERT INTO user_role (user_id, role_id)
SELECT id, 3 FROM users WHERE username = 'marius.stan';  -- Restaurant Owner
INSERT INTO user_role (user_id, role_id)
SELECT id, 2 FROM users WHERE username = 'georgiana.ilie';  -- User
INSERT INTO user_role (user_id, role_id)
SELECT id, 3 FROM users WHERE username = 'ionut.dumitru';  -- Restaurant Owner
INSERT INTO user_role (user_id, role_id)
SELECT id, 1 FROM users WHERE username = 'robert.mocanu';  -- Admin
INSERT INTO user_role (user_id, role_id)
SELECT id, 2 FROM users WHERE username = 'cristina.neagu';  -- User
INSERT INTO user_role (user_id, role_id)
SELECT id, 3 FROM users WHERE username = 'doru.pavel';  -- Restaurant Owner
INSERT INTO user_role (user_id, role_id)
SELECT id, 2 FROM users WHERE username = 'ana.marinescu';  -- User
INSERT INTO user_role (user_id, role_id)
SELECT id, 1 FROM users WHERE username = 'florin.radu';  -- Admin
