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

---

# PapoteCar — Backend Spring Boot

## Modèles JPA

Les entités sont dans `src/main/java/com/PapoteCar/PapoteCar/model/` et mappées sur la base MySQL via Spring Data JPA.

| Entité        | Table SQL       | Description                              |
|---------------|-----------------|------------------------------------------|
| `Utilisateur` | `utilisateurs`  | Compte utilisateur (nom, email, mdp...) |
| `Voiture`     | `voitures`      | Véhicule associé à un utilisateur        |
| `Trajet`      | `trajets`       | Trajet proposé par un conducteur         |
| `Reservation` | `reservations`  | Réservation d'un passager sur un trajet  |

Chaque entité dispose d'une interface JPA dans `repository/` qui étend `JpaRepository` et fournit automatiquement les opérations CRUD (save, findById, findAll, delete...).

Ces repositories sont exposés automatiquement en REST via `spring-boot-starter-data-rest` sur les routes `/utilisateurs`, `/voitures`, `/trajets`, `/reservations`.

---

## Authentification JWT

### Fonctionnement

1. L'utilisateur s'inscrit ou se connecte via `/auth/register` ou `/auth/login`
2. Le serveur retourne un **token JWT** signé
3. Le token doit être envoyé dans le header `Authorization` pour accéder aux routes protégées
4. Le token **expire automatiquement à minuit** du jour de sa création (fuseau Europe/Paris)

### Fichiers clés

| Fichier | Rôle |
|---|---|
| `security/JwtUtil.java` | Génération, validation et lecture des tokens |
| `security/JwtAuthFilter.java` | Filtre HTTP qui vérifie le token à chaque requête |
| `config/SecurityConfig.java` | Définit les routes publiques et protégées |
| `service/AuthService.java` | Logique métier register/login |
| `controller/AuthController.java` | Endpoints REST `/auth/register` et `/auth/login` |

### Routes publiques (pas de token requis)

| Méthode | Route            |
|---------|------------------|
| POST    | `/auth/register` |
| POST    | `/auth/login`    |

### Routes protégées (token requis)

Toutes les autres routes, dont `/utilisateurs`, `/voitures`, `/trajets`, `/reservations`.

---

## Tester avec Postman — Guide pas à pas

### Étape 1 — GET /utilisateurs sans token (doit échouer)

Vérifie que les routes protégées sont bien sécurisées.

- **Méthode :** `GET`
- **URL :** `http://localhost:8080/utilisateurs`
- **Headers :** aucun
- **Résultat attendu :** `403 Forbidden`

---

### Étape 2 — POST /auth/login (récupérer un token)

- **Méthode :** `POST`
- **URL :** `http://localhost:8080/auth/login`
- **Headers :**

| Key            | Value            |
|----------------|------------------|
| `Content-Type` | `application/json` |

- **Body** → `raw` → `JSON` :

```json
{
    "email": "alice@example.com",
    "motDePasse": "Password1!"
}
```

- **Résultat attendu :** `200 OK`

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expireA": "Minuit le 2026-03-10"
}
```

> Copie la valeur du champ `token`, tu en auras besoin à l'étape suivante.

---

### Étape 3 — GET /utilisateurs avec token (doit passer)

- **Méthode :** `GET`
- **URL :** `http://localhost:8080/utilisateurs`
- **Headers :**

| Key             | Value                          |
|-----------------|-------------------------------|
| `Authorization` | `Bearer eyJhbGciOiJIUzI1NiJ9...` |

> Remplace `eyJhbGciOiJIUzI1NiJ9...` par le token copié à l'étape 2.

- **Résultat attendu :** `200 OK` avec la liste des utilisateurs

---

### Astuce Postman — onglet Auth

Au lieu de saisir le header manuellement, utilise l'onglet **Auth** dans Postman :
- Type : `Bearer Token`
- Token : colle uniquement la valeur du token (sans le mot `Bearer`)
