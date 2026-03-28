package com.example.auth.service;

import java.util.UUID;

/**
 * Service pour générer des nonces aléatoires.
 * Le nonce est un UUID aléatoire utilisé dans le protocole SSO TP3.
 * Il empêche la réutilisation des preuves HMAC (protection anti-rejeu).
 */
public class NonceGenerator {

    private NonceGenerator() {}

    /**
     * Génère un nonce UUID aléatoire.
     *
     * @return un UUID en tant que String (sans tirets)
     */
    public static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

