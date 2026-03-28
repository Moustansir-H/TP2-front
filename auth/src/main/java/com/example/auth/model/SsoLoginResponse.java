package com.example.auth.model;

/**
 * Réponse d'authentification SSO TP3.
 * Contient le token d'accès et sa date d'expiration.
 */
public record SsoLoginResponse(
        String message,
        String email,
        String accessToken,
        String expiresAt
) {}

