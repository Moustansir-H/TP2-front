package com.example.auth.service;

import java.util.prefs.Preferences;

/**
 * Service pour persister et restaurer le token d'accès SSO.
 * Utilise les préférences du système (Preferences API) pour stocker le token
 * de manière sécurisée et persistante.
 */
public class TokenPersistenceService {

    private static final String PREFS_NODE = "com/example/auth";
    private static final String TOKEN_KEY = "accessToken";

    private TokenPersistenceService() {}

    /**
     * Sauvegarde le token d'accès.
     *
     * @param token le token à sauvegarder
     */
    public static void saveToken(String token) {
        Preferences prefs = Preferences.userNodeForPackage(TokenPersistenceService.class);
        if (token != null && !token.isBlank()) {
            prefs.put(TOKEN_KEY, token);
        } else {
            prefs.remove(TOKEN_KEY);
        }
    }

    /**
     * Récupère le token d'accès sauvegardé.
     *
     * @return le token ou null s'il n'existe pas
     */
    public static String getToken() {
        Preferences prefs = Preferences.userNodeForPackage(TokenPersistenceService.class);
        return prefs.get(TOKEN_KEY, null);
    }

    /**
     * Supprime le token d'accès (logout).
     */
    public static void clearToken() {
        Preferences prefs = Preferences.userNodeForPackage(TokenPersistenceService.class);
        prefs.remove(TOKEN_KEY);
    }
}

