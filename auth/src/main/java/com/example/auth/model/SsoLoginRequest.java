package com.example.auth.model;

/**
 * Requête de connexion SSO TP3.
 * Contient la preuve d'identité sans transmettre le mot de passe.
 */
public record SsoLoginRequest(
        String email,
        String nonce,
        Long timestamp,
        String hmac
) {}

