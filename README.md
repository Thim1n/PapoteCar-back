# PapoteCar — Base de données MySQL

Contient la configuration Docker pour la base de données MySQL du projet PapoteCar.

## Structure

```
docker/
├── docker-compose.yml
├── application.properties   ← à copier dans src/main/resources/
└── mysql/
    ├── Dockerfile
    └── initdb/
        ├── 00_charset.sql   # Configuration UTF-8
        ├── 01_schema.sql    # Création des tables
        └── 02_data.sql      # Données de test
```

## Démarrage

Depuis le dossier `docker/` :

```bash
# Construire l'image et démarrer la base de données
docker compose up -d --build
```

> La première fois, MySQL initialise la base et injecte les données (~20 secondes).
> Vérifier que le statut est `healthy` : `docker compose ps`

## Commandes utiles

```bash
# Démarrer (sans rebuild)
docker compose up -d

# Arrêter (conserve les données)
docker compose down

# Arrêter et supprimer toutes les données (reset complet)
docker compose down -v

# Rebuild complet + redémarrage (après modification des scripts SQL)
docker compose down -v && docker compose up -d --build
```

## Vérification des données

Se connecter au shell MySQL :

```bash
docker exec -it papotecar-db mysql -u papotecar_user -ppapotecar_pass papotecar
```

Vérifier que les tables sont bien remplies :

```sql
-- Liste des tables
SHOW TABLES;

-- 5 utilisateurs attendus
SELECT id, prenom, nom, email FROM utilisateurs;

-- 5 voitures attendues
SELECT id, utilisateur_id, modele FROM voitures;

-- 6 trajets attendus (actif / termine / annule)
SELECT id, depart_ville, arrivee_ville, statut FROM trajets;

-- 9 réservations attendues
SELECT id, trajet_id, passager_id, statut FROM reservations;

-- Quitter
EXIT;
```

## Connexion Spring Boot

Copier `application.properties` dans `src/main/resources/` ou ajouter les propriétés suivantes :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/papotecar?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris&characterEncoding=UTF-8
spring.datasource.username=papotecar_user
spring.datasource.password=papotecar_pass
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
```

## Identifiants

| Paramètre        | Valeur           |
|------------------|------------------|
| Hôte             | `localhost:3306` |
| Base de données  | `papotecar`      |
| Utilisateur      | `papotecar_user` |
| Mot de passe     | `papotecar_pass` |

> Mot de passe des comptes de test : `Password1!`
