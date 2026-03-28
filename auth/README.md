# Auth Front TP4 (JavaFX FXML)

Client JavaFX pour l'authentification SSO (HMAC nonce/timestamp) compatible avec l'API TP4.

## Ce qui change en TP4

- Le protocole login SSO reste identique (`email`, `nonce`, `timestamp`, `hmac`).
- La variable d'environnement `APP_MASTER_KEY` devient obligatoire au demarrage du front.
- Une pipeline GitHub Actions est ajoutee dans `../.github/workflows/ci.yml`:
  - build Maven
  - tests JUnit
  - analyse SonarCloud
  - echec si Quality Gate echoue

## Etape 1 - Pre-requis

- Java 17+
- Maven (ou `mvnw.cmd`)
- Backend lance (ex: `http://localhost:8080`)

## Etape 2 - Definir APP_MASTER_KEY

> Sans cette variable, l'application refuse de demarrer.

### Windows PowerShell (session courante)

```powershell
$env:APP_MASTER_KEY="ma_cle_master_locale"
```

### Windows PowerShell (persistant utilisateur)

```powershell
setx APP_MASTER_KEY "ma_cle_master_locale"
```

## Etape 3 - Lancer le front

```powershell
cd C:\Users\hassa\IdeaProjects\TP2-front\auth
.\mvnw.cmd javafx:run
```

## Etape 4 - Lancer les tests

```powershell
cd C:\Users\hassa\IdeaProjects\TP2-front\auth
$env:APP_MASTER_KEY="test_master_key_local"
.\mvnw.cmd test
```

## Etape 5 - Configurer GitHub Actions + SonarCloud

Le workflow est dans `C:\Users\hassa\IdeaProjects\TP2-front\.github\workflows\ci.yml`.

Dans GitHub (`Settings -> Secrets and variables -> Actions`), ajouter:

- `SONAR_TOKEN`
- `SONAR_PROJECT_KEY`
- `SONAR_ORGANIZATION`

Le workflow:

- se declenche sur `push` et `pull_request` vers `main`
- injecte une `APP_MASTER_KEY` fictive pour la CI
- execute `clean verify`
- lance SonarCloud
- bloque le merge si Quality Gate est rouge

## Configuration backend dans l'UI

- `Backend URL`: adresse du backend (ex: `http://localhost:8080`)
- `Register path`: endpoint d'inscription (ex: `/api/auth/register`)
- `Login path`: endpoint de login (ex: `/api/auth/login`)
- `SSO TP3`:
  - coche = payload SSO (`email`, `nonce`, `timestamp`, `hmac`)
  - decoche = payload classique (`email`, `password`)

## Regle mot de passe cote client

Mot de passe valide si:

- au moins 12 caracteres
- au moins 1 majuscule
- au moins 1 minuscule
- au moins 1 chiffre
- au moins 1 caractere special

## Fichiers principaux

- `src/main/resources/com/example/auth/auth-view.fxml`
- `src/main/java/com/example/auth/controller/AuthController.java`
- `src/main/java/com/example/auth/service/AuthApiClient.java`
- `src/main/java/com/example/auth/service/AppMasterKeyService.java`
- `src/main/java/com/example/auth/service/HmacProofService.java`
- `src/main/java/com/example/auth/service/TokenPersistenceService.java`
- `src/test/java/com/example/auth/service/HmacProofServiceTest.java`
- `src/test/java/com/example/auth/service/AppMasterKeyServiceTest.java`
- `../.github/workflows/ci.yml`
