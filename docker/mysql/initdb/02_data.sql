-- =============================================================
-- PapoteCar - Script de données initiales (jeu de test)
-- Mots de passe encodés BCrypt (valeur : "Test123")
-- =============================================================

USE papotecar;

-- -------------------------------------------------------------
-- Utilisateurs (5 conducteurs / passagers)
-- -------------------------------------------------------------
INSERT INTO utilisateurs (nom, prenom, username, email, mot_de_passe, tel, permis_de_conduire, solde) VALUES
('Dupont',   'Alice',   'alice.d',   'alice.dupont@email.com',   '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0601020304', 1, 100.00),
('Martin',   'Bob',     'bob.m',     'bob.martin@email.com',     '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0611223344', 1,  75.00),
('Bernard',  'Camille', 'camille.b', 'camille.bernard@email.com','$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0622334455', 0,  50.00),
('Leroy',    'David',   'david.l',   'david.leroy@email.com',    '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0633445566', 0,  40.00),
('Moreau',   'Emma',    'emma.m',    'emma.moreau@email.com',    '$2a$10$Lj2TvhV3ne5.CWpNXSaxzOEPGSww54osDrL.DHitrW8k9smZkSk/i', '0644556677', 0,  60.00);

-- -------------------------------------------------------------
-- Voitures
-- -------------------------------------------------------------
INSERT INTO voitures (utilisateur_id, modele, nb_passagers, taille_coffre) VALUES
(1, 'Renault Zoé',         3, 338),
(2, 'Peugeot e-208',       3, 311),
(3, 'Tesla Model 3',       4, 594),
(4, 'Volkswagen ID.4',     4, 543),
(5, 'Toyota Yaris Cross',  3, 397);

-- -------------------------------------------------------------
-- Trajets (DATE_ADD imbriqués — syntaxe MySQL valide)
-- -------------------------------------------------------------
INSERT INTO trajets (
    conducteur_id, voiture_id,
    depart_rue, depart_ville, depart_code_postal, depart_latitude, depart_longitude,
    arrivee_rue, arrivee_ville, arrivee_code_postal, arrivee_latitude, arrivee_longitude,
    horaire_depart, horaire_arrivee, temps_trajet_min, places_disponibles, prix, statut
) VALUES

-- Trajet 1 : Alice  Paris -> Lyon (actif, demain)
(1, 1,
 '10 Rue de Rivoli', 'Paris', '75001', 48.857000, 2.351000,
 '1 Place Bellecour', 'Lyon', '69002', 45.757800, 4.832000,
 DATE_ADD(NOW(), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 120 MINUTE),
 120, 2, 15.00, 'actif'),

-- Trajet 2 : Bob  Lyon -> Marseille (actif, dans 3 jours)
(2, 2,
 '5 Rue de la Republique', 'Lyon', '69001', 45.763600, 4.835700,
 '20 La Canebiere', 'Marseille', '13001', 43.296400, 5.381000,
 DATE_ADD(NOW(), INTERVAL 3 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 115 MINUTE),
 115, 3, 12.00, 'actif'),

-- Trajet 3 : Camille  Bordeaux -> Toulouse (actif, dans 2 jours)
(3, 3,
 '3 Allees de Tourny', 'Bordeaux', '33000', 44.841200, -0.580900,
 '8 Place du Capitole', 'Toulouse', '31000', 43.604500, 1.444100,
 DATE_ADD(NOW(), INTERVAL 2 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 130 MINUTE),
 130, 2, 10.00, 'actif'),

-- Trajet 4 : David  Paris -> Bordeaux (termine, passe)
(4, 4,
 '15 Avenue des Champs-Elysees', 'Paris', '75008', 48.869600, 2.307700,
 '1 Cours du Chapeau Rouge', 'Bordeaux', '33000', 44.836100, -0.570600,
 DATE_SUB(NOW(), INTERVAL 5 DAY),
 DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 220 MINUTE),
 220, 0, 20.00, 'termine'),

-- Trajet 5 : Emma  Nice -> Lyon (annule)
(5, 5,
 '7 Avenue Jean Medecin', 'Nice', '06000', 43.707300, 7.261700,
 '2 Place des Terreaux', 'Lyon', '69001', 45.767900, 4.833600,
 DATE_ADD(NOW(), INTERVAL 4 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 210 MINUTE),
 210, 3, 18.00, 'annule'),

-- Trajet 6 : Alice  Paris -> Nantes (actif, dans 7 jours)
(1, 1,
 '10 Rue de Rivoli', 'Paris', '75001', 48.857000, 2.351000,
 '1 Place du Commerce', 'Nantes', '44000', 47.213200, -1.553600,
 DATE_ADD(NOW(), INTERVAL 7 DAY),
 DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 140 MINUTE),
 140, 2, 14.00, 'actif');

-- -------------------------------------------------------------
-- Reservations (tous les statuts representes)
-- -------------------------------------------------------------
INSERT INTO reservations (trajet_id, passager_id, statut) VALUES
(1, 2, 'valide'),
(1, 3, 'en_attente'),
(2, 1, 'en_attente'),
(2, 5, 'refuse'),
(3, 4, 'valide'),
(4, 2, 'valide'),
(4, 5, 'valide'),
(6, 4, 'annule'),
(6, 3, 'en_attente');
