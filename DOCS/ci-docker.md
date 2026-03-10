# CI: build and publish Docker image

Ce document explique comment la GitHub Action fournie build et publie une image Docker de l'application Java vers GitHub Container Registry (GHCR).

Fichiers ajoutés:
- `docker/app/Dockerfile` : Dockerfile multi-stage (Maven build -> runtime image).
- `.github/workflows/docker-image.yml` : workflow GitHub Actions qui build et push l'image vers `ghcr.io`.

Comportement:
- Lors d'un push sur `master` (branche par défaut dans le workflow), l'action build l'image à partir du contexte racine (`.`) en utilisant `docker/app/Dockerfile`.
- L'image est taggée `ghcr.io/<owner>/<repo>:latest` et `ghcr.io/<owner>/<repo>:<sha>` puis poussée sur GHCR.

Permissions et secrets:
- Le workflow utilise `${{ secrets.GITHUB_TOKEN }}` pour s'authentifier auprès de `ghcr.io`. Assurez-vous que le repository dispose de la permission `packages: write` (définie dans le workflow via `permissions:`). Si votre organisation impose des restrictions, activez l'accès au `GITHUB_TOKEN` pour GitHub Packages dans les paramètres du repo/organization.

Utilisation locale / alternatives:
- Pour builder localement :
```
docker build -f docker/app/Dockerfile -t myrepo/mypapotecar:local .
```
- Pour pousser sur Docker Hub à la place de GHCR, créez des secrets `DOCKERHUB_USERNAME` et `DOCKERHUB_TOKEN` et modifiez l'étape de login et les tags dans le workflow (ou remplacez `ghcr.io/${{ github.repository }}` par `${{ secrets.DOCKERHUB_USERNAME }}/papotecar`).

Notes:
- Le Dockerfile utilise Maven container pour builder l'artifact. Le build dans l'action se fait via Docker (pas besoin de Java sur l'instance runner).
- Ajustez `JAVA_OPTS`, la base JRE ou la version de Java selon vos besoins (pom.xml peut exiger une version spécifique).
