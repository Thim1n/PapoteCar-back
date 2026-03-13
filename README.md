# PapoteCar — Backend API

Application de covoiturage. API REST Java/Spring Boot avec authentification JWT.

---

## Stack technique

- **Java 21** / **Spring Boot 4.0.3**
- **Maven** (`./mvnw`)
- **MySQL 8.0** via Docker
- **Spring Security** + **JWT** (jjwt 0.12.6)
- **Spring Data JPA** + **Spring Data REST**
- **Lombok**
- **Swagger / OpenAPI** (`springdoc-openapi`) — `http://localhost:8080/swagger-ui.html`

---

## Démarrage rapide

```bash
git clone https://github.com/Thim1n/PapoteCar-back.git
#recupperer les images build depuis github
docker compose pull 

docker compose up -d --build
```
> Première fois : MySQL initialise la base et injecte les données (~20 secondes).
> Vérifier le statut : `docker compose ps` (doit être `healthy`)

### 2. Application Spring Boot

```bash
./mvnw spring-boot:run
```

L'API est disponible sur `http://localhost:8080`.

---

## Base de données

| Paramètre       | Valeur           |
|-----------------|------------------|
| Hôte            | `localhost:3306` |
| Base            | `papotecar`      |
| Utilisateur     | `papotecar_user` |
| Mot de passe    | `papotecar_pass` |

### Connexion MySQL

```bash
docker exec -it papotecar-db mysql -u papotecar_user -ppapotecar_pass papotecar
```

### Données de test

| Table          | Contenu attendu                           |
|----------------|-------------------------------------------|
| `utilisateurs` | 3 utilisateurs (mot de passe : `Test123`) |
| `voitures`     | 2 voitures                                |
| `trajets`      | 5 trajets (actif / terminé / annulé)      |
| `reservations` | 1 réservations                            |

---

## Structure du code

```
src/main/java/com/PapoteCar/PapoteCar/
├── controller/    # REST controllers (auth, user, voiture, trajet, réservation)
├── dto/           # Request / Response DTOs
├── model/         # Entités JPA
├── repository/    # Spring Data repositories
├── security/      # JwtUtil, JwtAuthFilter
├── service/       # AuthService (register/login uniquement)
└── config/        # SecurityConfig, OpenApiConfig, WebConfig
```

---

## Modèles de données

| Entité        | Table          | Relations                                              |
|---------------|----------------|--------------------------------------------------------|
| `Utilisateur` | `utilisateurs` | —                                                      |
| `Voiture`     | `voitures`     | ManyToOne → Utilisateur                                |
| `Trajet`      | `trajets`      | ManyToOne → Utilisateur (conducteur), ManyToOne → Voiture |
| `Reservation` | `reservations` | ManyToOne → Trajet, ManyToOne → Utilisateur (passager) |

---

## Routes API

### Authentification (public)

| Méthode | Route            | Description              |
|---------|------------------|--------------------------|
| POST    | `/auth/register` | Créer un compte          |
| POST    | `/auth/login`    | Connexion — retourne JWT |

Toutes les autres routes nécessitent `Authorization: Bearer <token>`.

---

### Utilisateur — `/user`

| Méthode | Route               | Description                                               |
|---------|---------------------|-----------------------------------------------------------|
| GET     | `/user/me`          | Profil complet de l'utilisateur connecté                  |
| POST    | `/user/me/permis`   | Upload permis de conduire (form-data, champ `fichier`) — passe `permisDeConduire` à `true` |
| GET     | `/user/{id}`        | Profil d'un utilisateur (email/tel/solde masqués si ce n'est pas soi) |
| PATCH   | `/user/{id}`        | Modifier son profil (`nom`, `prenom`, `tel`)              |
| GET     | `/user/{id}/trajets`| Trajets créés par cet utilisateur (conducteur, tous statuts) |
| DELETE  | `/user/{id}`        | Supprimer son compte (interdit si trajet actif en cours)  |

---

### Voiture

| Méthode | Route           | Description                                                   |
|---------|-----------------|---------------------------------------------------------------|
| GET     | `/voitures`     | Mes voitures                                                  |
| GET     | `/voiture/{id}` | Détail d'une voiture (propriétaire uniquement)                |
| POST    | `/voitures`     | Créer une voiture (`modele`, `nbPassagers` obligatoires)      |
| PATCH   | `/voiture/{id}` | Modifier une voiture (interdit si trajet actif l'utilise)     |
| DELETE  | `/voiture/{id}` | Supprimer une voiture (interdit si trajet actif l'utilise)    |

---

### Trajet

| Méthode | Route                      | Description                                                      |
|---------|----------------------------|------------------------------------------------------------------|
| GET     | `/trajets/mes-trajets`     | Mes trajets en tant que conducteur (tous statuts)                |
| GET     | `/trajets/mes-reservations`| Mes trajets réservés en tant que passager                        |
| GET     | `/trajets`                 | Rechercher des trajets actifs (filtres optionnels, voir ci-dessous) |
| GET     | `/trajets/{id}`            | Détail complet d'un trajet                                       |
| POST    | `/trajets`                 | Créer un trajet (permis de conduire requis)                      |
| PATCH   | `/trajets/{id}`            | Modifier un trajet actif (PATCH partiel)                         |
| DELETE  | `/trajets/{id}`            | Annuler un trajet (soft delete → statut `annule`)                |
| PUT     | `/trajets/{id}/terminer`   | Passer le trajet en statut `termine`                             |

#### Filtres de recherche `GET /trajets`

**Mode texte** (paramètres optionnels) :

| Paramètre    | Type     | Exemple        | Description                    |
|--------------|----------|----------------|--------------------------------|
| `departVille`  | string   | `Paris`        | Ville de départ                |
| `arriveeVille` | string   | `Lyon`         | Ville d'arrivée                |
| `date`         | date ISO | `2026-04-01`   | Date du trajet                 |
| `placesMin`    | integer  | `2`            | Nombre de places minimum       |

**Mode GPS** (activé dès que `departLat`+`departLon` ou `arriveeLat`+`arriveeLon` sont présents) :

| Paramètre    | Type   | Exemple   | Description                           |
|--------------|--------|-----------|---------------------------------------|
| `departLat`    | double | `48.8566` | Latitude du point de départ           |
| `departLon`    | double | `2.3522`  | Longitude du point de départ          |
| `arriveeLat`   | double | `45.7640` | Latitude du point d'arrivée           |
| `arriveeLon`   | double | `4.8357`  | Longitude du point d'arrivée          |
| `rayonKm`      | double | `20`      | Rayon de recherche en km (défaut: 20) |

---

### Réservation

| Méthode | Route                            | Rôle       | Description                                                              |
|---------|----------------------------------|------------|--------------------------------------------------------------------------|
| POST    | `/trajets/{id}/reservations`     | Passager   | S'inscrire sur un trajet (statut `en_attente`)                           |
| GET     | `/trajets/{id}/reservations`     | Conducteur | Liste des passagers du trajet avec leur statut                           |
| GET     | `/reservations/{id}`             | Les deux   | Détail d'une réservation (passager OU conducteur du trajet)              |
| DELETE  | `/reservations/{id}`             | Passager   | Annuler sa réservation (remboursement solde si statut était `valide`)    |
| PUT     | `/reservations/{id}/valider`     | Conducteur | Accepter un passager — décrémente places et débite le solde du passager  |
| PUT     | `/reservations/{id}/refuser`     | Conducteur | Refuser un passager en attente                                           |

#### Cycle de vie d'une réservation

```
en_attente → valide    (conducteur valide)
           → refuse    (conducteur refuse)
           → annule    (passager annule)
valide     → annule    (passager annule — remboursement)
```

---

## Authentification JWT

1. L'utilisateur se connecte via `POST /auth/login`
2. Le serveur retourne un **token JWT** + date d'expiration (minuit du jour en cours, fuseau Europe/Paris)
3. Le token doit être envoyé dans le header `Authorization: Bearer <token>` pour toutes les routes protégées

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expireA": "Minuit le 2026-03-13"
}
```
---

## Documentation Swagger

Disponible en local une fois l'application lancée :

```
http://localhost:8080/swagger-ui.html
```

---

## Configuration Spring Boot

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/papotecar?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris&characterEncoding=UTF-8
spring.datasource.username=papotecar_user
spring.datasource.password=papotecar_pass
spring.jpa.hibernate.ddl-auto=validate
```

Le `jwt.secret` est configuré dans `src/main/resources/application.properties`.