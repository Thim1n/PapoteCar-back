-- =============================================================
-- PapoteCar — Jeu de données de test (Version Corrigée)
-- Mot de passe BCrypt : "Test123"
-- =============================================================
USE papotecar;

-- Nettoyage préalable (Optionnel, utile pour relancer le script souvent)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reservations;
TRUNCATE TABLE trajets;
TRUNCATE TABLE voitures;
TRUNCATE TABLE utilisateurs;
SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------------------
-- Utilisateurs
-- -------------------------------------------------------------
INSERT INTO utilisateurs (nom, prenom, username, email, mot_de_passe, tel, permis_de_conduire, solde) VALUES
-- user1 : conducteur principal (Alice)
('Martin',  'Alice',  'alice',  'alice@papotecar.fr',  '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0601020304', 1, 200.00),
-- user2 : passager solvable (Bob)
('Dupont',  'Bob',    'bob',    'bob@papotecar.fr',    '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0611223344', 0,  50.00),
-- user3 : passager non solvable (Claire)
('Leroy',   'Claire', 'claire', 'claire@papotecar.fr', '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0622334455', 0,   5.00);

-- -------------------------------------------------------------
-- Voitures (Propriétaire : user1)
-- -------------------------------------------------------------
INSERT INTO voitures (utilisateur_id, modele, nb_passagers, couleur, taille_coffre) VALUES
        (1, 'Renault Zoé',  3, 'Blanche', 'Moyen'),
        (1, 'Peugeot 208',  2, 'Grise',   'Petit'); -- ID 2

-- -------------------------------------------------------------
-- Trajets
-- -------------------------------------------------------------
INSERT INTO trajets (
    conducteur_id, voiture_id,
    depart_rue, depart_ville, depart_code_postal, depart_latitude, depart_longitude,
    arrivee_rue, arrivee_ville, arrivee_code_postal, arrivee_latitude, arrivee_longitude,
    horaire_depart, horaire_arrivee, temps_trajet_min, places_disponibles, prix, statut
) VALUES

-- ── trajet1 : ACTIF — Paris→Lyon — +2 jours
(1, 1,
    '10 Rue de Rivoli',    'Paris', '75001', 48.857000,  2.351000,
    '1 Place Bellecour',   'Lyon',  '69002', 45.757800,  4.832000,
    DATE_ADD(NOW(), INTERVAL 2 DAY),
    DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 120 MINUTE),
    120, 2, 15.00, 'actif'),

-- ── trajet2 : TERMINÉ — Paris→Marseille — -5 jours
(1, 2,
 '15 Av. des Champs-Elysées', 'Paris',     '75008', 48.869600,  2.307700,
 '20 La Canebière',           'Marseille', '13001', 43.296400,  5.381000,
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 115 MINUTE),
 115, 0, 10.00, 'termine'),

-- ── trajet3 : ANNULÉ — Paris→Bordeaux — +3 jours
(1, 2,
 '10 Rue de Rivoli',       'Paris',    '75001', 48.857000,  2.351000,
 '1 Cours du Chapeau Rouge','Bordeaux', '33000', 44.836100, -0.570600,
 DATE_ADD(NOW(), INTERVAL 3 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 200 MINUTE),
 200, 2, 12.00, 'annule'),

-- ── trajet4 : ACTIF — Paris→Nantes — +4 jours (COMPLET)
(1, 1,
 '10 Rue de Rivoli',   'Paris',  '75001', 48.857000,  2.351000,
 '1 Place du Commerce','Nantes', '44000', 47.213200, -1.553600,
 DATE_ADD(NOW(), INTERVAL 4 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 140 MINUTE),
 140, 0, 20.00, 'actif'),

-- ── trajet5 : ACTIF — Paris→Strasbourg — +6 jours (VIERGE)
(1, 2,
 '10 Rue de Rivoli',  'Paris',      '75001', 48.857000,  2.351000,
 '1 Place Kléber',    'Strasbourg', '67000', 48.583800,  7.747900,
 DATE_ADD(NOW(), INTERVAL 6 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 6 DAY), INTERVAL 130 MINUTE),
 130, 3,  8.00, 'actif');
-- -------------------------------------------------------------
-- Réservations
-- -------------------------------------------------------------
INSERT INTO reservations (trajet_id, passager_id, statut) VALUES
    (1, 2, 'en_attente'),
    (1, 3, 'en_attente'),
    (2, 2, 'valide'),
    (3, 2, 'annule'),
    (4, 3, 'en_attente');