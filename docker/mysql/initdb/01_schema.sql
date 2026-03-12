-- =============================================================
-- PapoteCar - Script d'initialisation de la base de données
-- MySQL 8.0 | Encodage UTF-8
-- =============================================================

CREATE DATABASE IF NOT EXISTS papotecar
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE papotecar;

-- -------------------------------------------------------------
-- Table : utilisateurs
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS utilisateurs (
    id           INT          NOT NULL AUTO_INCREMENT,
    nom          VARCHAR(100) NOT NULL,
    prenom       VARCHAR(100) NOT NULL,
    username     VARCHAR(100) NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    tel                VARCHAR(20),
    permis_de_conduire TINYINT(1)   NOT NULL DEFAULT 0,
    solde              DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Table : voitures
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS voitures (
    id              INT          NOT NULL AUTO_INCREMENT,
    utilisateur_id  INT          NOT NULL,
    modele          VARCHAR(100) NOT NULL,
    nb_passagers    INT          NOT NULL,
    couleur         VARCHAR(20),
    taille_coffre   INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_voiture_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Table : trajets
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS trajets (
    id                    INT             NOT NULL AUTO_INCREMENT,
    conducteur_id         INT             NOT NULL,
    voiture_id            INT             NOT NULL,

    -- Adresse départ
    depart_rue            VARCHAR(255),
    depart_ville          VARCHAR(100)    NOT NULL,
    depart_code_postal    VARCHAR(10),
    depart_latitude       DECIMAL(9, 6),
    depart_longitude      DECIMAL(9, 6),

    -- Adresse arrivée
    arrivee_rue           VARCHAR(255),
    arrivee_ville         VARCHAR(100)    NOT NULL,
    arrivee_code_postal   VARCHAR(10),
    arrivee_latitude      DECIMAL(9, 6),
    arrivee_longitude     DECIMAL(9, 6),

    horaire_depart        TIMESTAMP       NOT NULL,
    horaire_arrivee       TIMESTAMP       NOT NULL,
    temps_trajet_min      INT,
    places_disponibles    INT             NOT NULL DEFAULT 1,
    prix                  DECIMAL(6,2)    NOT NULL DEFAULT 0.00,
    statut                ENUM('actif','termine','annule') NOT NULL DEFAULT 'actif',
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_trajet_conducteur
        FOREIGN KEY (conducteur_id) REFERENCES utilisateurs(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_trajet_voiture
        FOREIGN KEY (voiture_id) REFERENCES voitures(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- Index utiles pour la recherche de trajets
    INDEX idx_trajet_depart_ville   (depart_ville),
    INDEX idx_trajet_arrivee_ville  (arrivee_ville),
    INDEX idx_trajet_horaire_depart (horaire_depart),
    INDEX idx_trajet_statut         (statut),
    INDEX idx_trajet_search         (depart_ville, arrivee_ville, horaire_depart, statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Table : reservations
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    id          INT  NOT NULL AUTO_INCREMENT,
    trajet_id   INT  NOT NULL,
    passager_id INT  NOT NULL,
    statut      ENUM('en_attente','valide','refuse','annule') NOT NULL DEFAULT 'en_attente',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_reservation_trajet
        FOREIGN KEY (trajet_id) REFERENCES trajets(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reservation_passager
        FOREIGN KEY (passager_id) REFERENCES utilisateurs(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- Un passager ne peut réserver qu'une seule fois le même trajet
    UNIQUE KEY uq_reservation (trajet_id, passager_id),

    INDEX idx_reservation_trajet   (trajet_id),
    INDEX idx_reservation_passager (passager_id),
    INDEX idx_reservation_statut   (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
