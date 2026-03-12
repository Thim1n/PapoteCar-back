-- =============================================================
-- PapoteCar — Jeu de données de test
-- Mot de passe BCrypt : "Test123"
-- =============================================================
--
-- STRUCTURE DU JEU DE DONNÉES
-- ─────────────────────────────────────────────────────────────
-- user1 (id=1) : conducteur principal — permis=1, solde=200.00
-- user2 (id=2) : passager riche       — permis=0, solde=50.00
-- user3 (id=3) : passager pauvre      — permis=0, solde=5.00
--
-- voiture1 (id=1) : user1 — 3 places — UTILISÉE dans trajet actif → 409 PATCH/DELETE
-- voiture2 (id=2) : user1 — 2 places — libre                      → OK  PATCH/DELETE
--
-- TRAJETS (tous conducteur=user1)
-- trajet1 (id=1) : actif   voiture1 Paris→Lyon        prix=15 places=2  → tests réservations
-- trajet2 (id=2) : terminé voiture2 Paris→Marseille   prix=10 places=0  → 409 PATCH/DELETE/terminer
-- trajet3 (id=3) : annulé  voiture2 Paris→Bordeaux    prix=12 places=2  → 409 DELETE/terminer/réserver
-- trajet4 (id=4) : actif   voiture1 Paris→Nantes      prix=20 places=0  → 409 réserver complet / 409 valider complet
-- trajet5 (id=5) : actif   voiture2 Paris→Strasbourg  prix=8  places=3  → trajet vierge POST réservation 201
--
-- RÉSERVATIONS
-- resa1 (id=1) : user2 / trajet1 / en_attente → PUT valider 200 (50≥15) · PUT refuser 200 · GET · DELETE annuler
-- resa2 (id=2) : user3 / trajet1 / en_attente → PUT valider 409 solde insuff (5<15)
-- resa3 (id=3) : user2 / trajet2 / valide     → DELETE réservation valide (remboursement +10 au solde)
-- resa4 (id=4) : user2 / trajet3 / annule     → DELETE 409 déjà annulée
-- resa5 (id=5) : user3 / trajet4 / en_attente → PUT valider 409 trajet complet (0 places)
--
-- CAS DE TEST COUVERTS PAR ROUTE
-- ─────────────────────────────────────────────────────────────
-- GET  /user/me                      200 (user1)
-- GET  /user/{id}                    200 propre profil (email+tel+solde visibles)
--                                    200 profil autre  (email+tel+solde masqués)
--                                    404 id inexistant
-- PATCH /user/{id}                   200 user1 → user1
--                                    403 user1 → user2
-- GET  /user/{id}/trajets            200 avec trajets (user1) · 200 vide (user2/user3)
-- DELETE /user/{id}                  409 user1 a trajet actif (trajet1, trajet4)
--                                    403 user1 supprime user2
--                                    204 user2 ou user3 supprime son propre compte (pas de trajet actif)
--
-- GET  /voitures                     200 liste (user1 : 2 voitures)
-- GET  /voiture/{id}                 200 · 403 non-propriétaire · 404
-- POST /voitures                     201
-- PATCH /voiture/{id}                409 voiture1 (trajet actif) · 200 voiture2 (libre) · 403
-- DELETE /voiture/{id}               409 voiture1 (trajet actif) · 200 voiture2 (libre) · 403
--
-- GET  /trajets                      200 sans filtres · 200 avec filtres ville · 200 avec GPS
-- GET  /trajets/mes-trajets          200 (user1 : 5 trajets)
-- GET  /trajets/{id}                 200 · 404
-- POST /trajets                      201 · 403 sans permis (user2/user3)
-- PATCH /trajets/{id}                200 actif · 409 terminé · 409 annulé · 403
-- DELETE /trajets/{id}               204 actif · 409 terminé · 409 annulé · 403
-- PUT  /trajets/{id}/terminer        200 actif · 409 terminé · 409 annulé · 403
--
-- POST /trajets/{id}/reservations    201 vierge (trajet5)
--                                    400 réserver son propre trajet (user1 sur trajet5)
--                                    409 trajet inactif (trajet2 terminé, trajet3 annulé)
--                                    409 déjà réservé (user2 tente resa sur trajet1 à nouveau)
--                                    409 complet (trajet4 places=0)
-- GET  /trajets/{id}/reservations    200 conducteur (user1 sur trajet1) · 403 non-conducteur
-- GET  /reservations/{id}            200 passager (user2 → resa1) · 200 conducteur (user1 → resa1) · 403 tiers
-- DELETE /reservations/{id}          204 en_attente sans remboursement (resa1)
--                                    204 valide avec remboursement    (resa3 user2+10)
--                                    409 déjà annulée                  (resa4)
--                                    403 non-passager
-- PUT  /reservations/{id}/valider    200 en_attente OK (resa1 : user2 solde 50≥15)
--                                    409 solde insuff  (resa2 : user3 solde 5<15)
--                                    409 trajet complet (resa5 : trajet4 places=0)
--                                    409 déjà validée/refusée
--                                    403 non-conducteur
-- PUT  /reservations/{id}/refuser    200 en_attente (resa2) · 409 déjà refusée · 403
-- =============================================================

USE papotecar;

-- -------------------------------------------------------------
-- Utilisateurs
-- -------------------------------------------------------------
INSERT INTO utilisateurs (nom, prenom, username, email, mot_de_passe, tel, permis_de_conduire, solde) VALUES
-- user1 : conducteur principal (permis + solde élevé)
('Martin',  'Alice',  'alice',  'alice@papotecar.fr',  '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0601020304', 1, 200.00),
-- user2 : passager riche (solde 50 > prix trajet1=15 → validation OK)
('Dupont',  'Bob',    'bob',    'bob@papotecar.fr',    '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0611223344', 0,  50.00),
-- user3 : passager pauvre (solde 5 < prix trajet1=15 → validation 409)
('Leroy',   'Claire', 'claire', 'claire@papotecar.fr', '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0622334455', 0,   5.00);

-- -------------------------------------------------------------
-- Voitures (propriétaire : user1)
-- -------------------------------------------------------------
INSERT INTO voitures (utilisateur_id, modele, nb_passagers, couleur, taille_coffre) VALUES
-- voiture1 : utilisée dans trajet1 (actif) et trajet4 (actif) → PATCH/DELETE → 409
(1, 'Renault Zoé',  3, 'Blanche', 'Moyen'),
-- voiture2 : utilisée dans trajet2 (terminé) et trajet3 (annulé) → PATCH/DELETE → 200
(1, 'Peugeot 208',  2, 'Grise',   'Petit');

-- -------------------------------------------------------------
-- Trajets (conducteur + voiture : user1)
-- -------------------------------------------------------------
INSERT INTO trajets (
    conducteur_id, voiture_id,
    depart_rue, depart_ville, depart_code_postal, depart_latitude, depart_longitude,
    arrivee_rue, arrivee_ville, arrivee_code_postal, arrivee_latitude, arrivee_longitude,
    horaire_depart, horaire_arrivee, temps_trajet_min, places_disponibles, prix, statut
) VALUES

-- ── trajet1 : ACTIF — voiture1 — Paris→Lyon — prix=15 — places=2
-- Cas couverts : socle de tous les tests réservation, PATCH trajet 200,
--               409 DELETE user1 (trajet actif), 409 PATCH/DELETE voiture1
(1, 1,
 '10 Rue de Rivoli',    'Paris', '75001', 48.857000,  2.351000,
 '1 Place Bellecour',   'Lyon',  '69002', 45.757800,  4.832000,
 DATE_ADD(NOW(), INTERVAL  2 DAY),
 DATE_ADD(NOW(), INTERVAL  2 DAY  + INTERVAL 120 MINUTE),
 120, 2, 15.00, 'actif'),

-- ── trajet2 : TERMINÉ — voiture2 — Paris→Marseille — prix=10 — places=0
-- Cas couverts : 409 PATCH trajet, 409 DELETE trajet, 409 PUT/terminer,
--               409 POST réservation sur trajet inactif,
--               voiture2 libre pour PATCH/DELETE voiture
(1, 2,
 '15 Av. des Champs-Elysées', 'Paris',     '75008', 48.869600,  2.307700,
 '20 La Canebière',           'Marseille', '13001', 43.296400,  5.381000,
 DATE_SUB(NOW(), INTERVAL  5 DAY),
 DATE_SUB(NOW(), INTERVAL  5 DAY  - INTERVAL 115 MINUTE),
 115, 0, 10.00, 'termine'),

-- ── trajet3 : ANNULÉ — voiture2 — Paris→Bordeaux — prix=12 — places=2
-- Cas couverts : 409 DELETE trajet annulé, 409 PUT/terminer trajet annulé,
--               409 POST réservation sur trajet inactif
(1, 2,
 '10 Rue de Rivoli',       'Paris',    '75001', 48.857000,  2.351000,
 '1 Cours du Chapeau Rouge','Bordeaux', '33000', 44.836100, -0.570600,
 DATE_ADD(NOW(), INTERVAL  3 DAY),
 DATE_ADD(NOW(), INTERVAL  3 DAY  + INTERVAL 200 MINUTE),
 200, 2, 12.00, 'annule'),

-- ── trajet4 : ACTIF — voiture1 — Paris→Nantes — prix=20 — places=0 (COMPLET)
-- Cas couverts : 409 POST réservation (complet), 409 PUT/valider (places=0),
--               renforce 409 PATCH/DELETE voiture1 (2ème trajet actif)
(1, 1,
 '10 Rue de Rivoli',   'Paris',  '75001', 48.857000,  2.351000,
 '1 Place du Commerce','Nantes', '44000', 47.213200, -1.553600,
 DATE_ADD(NOW(), INTERVAL  4 DAY),
 DATE_ADD(NOW(), INTERVAL  4 DAY  + INTERVAL 140 MINUTE),
 140, 0, 20.00, 'actif'),

-- ── trajet5 : ACTIF — voiture2 — Paris→Strasbourg — prix=8 — places=3 (VIERGE)
-- Cas couverts : POST réservation 201 (aucune resa pré-existante),
--               PUT /terminer 200 (en fin de session test)
(1, 2,
 '10 Rue de Rivoli',  'Paris',      '75001', 48.857000,  2.351000,
 '1 Place Kléber',    'Strasbourg', '67000', 48.583800,  7.747900,
 DATE_ADD(NOW(), INTERVAL  6 DAY),
 DATE_ADD(NOW(), INTERVAL  6 DAY  + INTERVAL 130 MINUTE),
 130, 3,  8.00, 'actif');

-- -------------------------------------------------------------
-- Réservations
-- -------------------------------------------------------------
INSERT INTO reservations (trajet_id, passager_id, statut) VALUES

-- resa1 : user2 / trajet1 / en_attente
-- → GET 200 (passager user2 ou conducteur user1)
-- → PUT /valider 200  (user2 solde=50 ≥ prix=15 ✅)
-- → PUT /refuser 200
-- → DELETE 204 annulation sans remboursement (statut=en_attente)
-- → PUT /valider 409 après refus/validation (statut ≠ en_attente)
(1, 2, 'en_attente'),

-- resa2 : user3 / trajet1 / en_attente
-- → PUT /valider 409 solde insuffisant (user3 solde=5 < prix=15)
-- → PUT /refuser 200 (conducteur user1)
-- → GET 403 si tiers (ni user3 ni user1)
(1, 3, 'en_attente'),

-- resa3 : user2 / trajet2 / valide
-- → DELETE 204 annulation d'une réservation VALIDE → remboursement +10 au solde user2
-- → GET 200 (user2 ou user1)
(2, 2, 'valide'),

-- resa4 : user2 / trajet3 / annule
-- → DELETE 409 "déjà annulée"
-- → GET 200 (user2 ou user1)
(3, 2, 'annule'),

-- resa5 : user3 / trajet4 / en_attente (trajet4 a places=0)
-- → PUT /valider 409 "trajet complet" (places_disponibles=0)
-- → PUT /refuser 200 (conducteur user1)
(4, 3, 'en_attente');