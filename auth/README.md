# Auth Front TP3 (JavaFX FXML)

Client JavaFX FXML pour l'authentification:
- inscription
- login TP3 en mode SSO (email + nonce + timestamp + hmac)
- fallback login classique (email + password) via case a cocher
- validation email/mot de passe cote client
- confirmation du mot de passe
- indicateur de force (rouge / orange / vert)
- endpoints backend configurables dans l'UI

## Pre-requis

- Java 17+
- Maven (ou `mvnw.cmd`)
- Backend lance (ex: `http://localhost:8080`)

## Lancer le front

Depuis `TP2/auth-front/auth`:

```powershell
cd C:\Users\hassa\IdeaProjects\TP2-front\auth
.\mvnw.cmd javafx:run
```

## Verifier compilation/tests front

```powershell
cd C:\Users\hassa\IdeaProjects\TP2-front\auth
.\mvnw.cmd test
```

## Configuration backend dans l'UI

En haut de la fenetre:
- `Backend URL` : adresse du backend (ex: `http://localhost:8080`)
- `Register path` : endpoint d'inscription (ex: `/api/auth/register`)
- `Login path` : endpoint de login (ex: `/api/auth/login`)
- `SSO TP3` :
  - coche = payload TP3 (`email`, `nonce`, `timestamp`, `hmac`)
  - decoche = payload classique (`email`, `password`)

## Compatibilite reponse token

Le front essaye d'extraire le token depuis: `accessToken`, puis `token`, puis `jwt`.

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
- `src/main/java/com/example/auth/service/ClientValidationService.java`
- `src/main/java/com/example/auth/service/HmacProofService.java`
- `src/main/java/com/example/auth/service/TokenPersistenceService.java`
- `src/main/java/com/example/auth/HelloApplication.java`

