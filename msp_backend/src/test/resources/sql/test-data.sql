-- Données déterministes réservées à MspBackendApplicationTests.
-- Le profil test crée les tables avec Hibernate ; Flyway reste désactivé.
INSERT INTO _utilisateur
    (slug, date_creation, nom, prenom, genre, matricule, numero_inscription,
     password, pere, mere, enabled, account_locked, account_non_expired, role_id)
SELECT 'test-adherent-' || n, current_timestamp, 'TEST', 'Membre ' || n,
       CASE WHEN n % 2 = 0 THEN 'FEMME' ELSE 'HOMME' END,
       'TEST-MALI-' || lpad(n::text, 3, '0'), 'MSP-TEST-' || lpad(n::text, 3, '0'),
       'unused-test-password', 'Père test', 'Mère test', true, false, false,
       (SELECT id FROM _role WHERE nom = 'ADHERENT')
FROM generate_series(1, 20) n;

INSERT INTO _courrier
    (slug, date_creation, nom, objet, type_courrier, description, date_arrivee, date_depart)
SELECT 'test-courrier-' || n, current_timestamp, 'TEST-COURRIER-' || lpad(n::text, 3, '0'),
       'Courrier de test ' || n, CASE WHEN n % 2 = 0 THEN 'ARRIVE' ELSE 'DEPART' END,
       'Correspondant de test ' || n,
       CASE WHEN n % 2 = 0 THEN current_date ELSE NULL END,
       CASE WHEN n % 2 = 1 THEN current_date ELSE NULL END
FROM generate_series(1, 20) n;

INSERT INTO _pv (slug, date_creation, numero, objet, type, date_pv)
SELECT 'test-pv-' || n, current_timestamp, 'TEST-PV-' || lpad(n::text, 3, '0'),
       'Réunion de test ' || n, 'REUNION', current_date
FROM generate_series(1, 20) n;

INSERT INTO _plainte (slug, date_creation, date_pv, observation)
SELECT 'TEST-PLAINTE-' || lpad(n::text, 3, '0'), current_timestamp, current_date,
       'Plainte de test ' || n
FROM generate_series(1, 20) n;

INSERT INTO _convocation (slug, date_creation, date_convocation, description)
SELECT 'TEST-CONVOCATION-' || lpad(n::text, 3, '0'), current_timestamp, current_date,
       'Convocation de test ' || n
FROM generate_series(1, 20) n;
