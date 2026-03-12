package com.PapoteCar.PapoteCar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("PapoteCar API")
                        .description("""
                                API REST de covoiturage — authentification via JWT Bearer.

                                **Workflow d'authentification :**
                                1. `POST /auth/register` pour créer un compte (retourne un token JWT)
                                2. `POST /auth/login` pour se connecter
                                3. Cliquer sur **Authorize** et coller le token (sans le préfixe "Bearer ")
                                4. Toutes les routes protégées utiliseront automatiquement ce token

                                **Données de test disponibles (mot de passe : `Test123`) :**
                                | Email | Rôle | Particularité |
                                |---|---|---|
                                | alice@papotecar.fr | Conducteur | permis=true, solde=200€ |
                                | bob@papotecar.fr | Passager | solde=50€ |
                                | claire@papotecar.fr | Passager | solde=5€ (solde insuffisant) |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PapoteCar")
                                .email("contact@papotecar.fr")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenu via POST /auth/login")))
                .tags(List.of(
                        new Tag().name("Auth")
                                .description("Authentification — routes publiques (pas de token requis)"),
                        new Tag().name("Utilisateur")
                                .description("Gestion du profil utilisateur"),
                        new Tag().name("Voiture")
                                .description("Gestion des voitures — ownership vérifié sur toutes les routes"),
                        new Tag().name("Trajet")
                                .description("Création et recherche de trajets — permis de conduire requis pour créer"),
                        new Tag().name("Réservation")
                                .description("Cycle de vie d'une réservation : en_attente → valide/refuse → annule")
                ));
    }
}