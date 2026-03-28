package com.example.auth.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Service pour calculer et vérifier les preuves HMAC-SHA256.
 * Utilisé pour le protocole d'authentification SSO TP3.
 *
 * Message = email:nonce:timestamp
 * HMAC = HMAC_SHA256(key=password, data=message) encodé en Base64
 */
public class HmacProofService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Calcule l'HMAC-SHA256 et l'encode en Base64.
     *
     * @param key       la clé secrète (mot de passe en clair)
     * @param message   le message à signer (email:nonce:timestamp)
     * @return HMAC encodé en Base64
     */
    public static String computeBase64(String key, String message) {
        return Base64.getEncoder().encodeToString(computeBytes(key, message));
    }

    /**
     * Vérifie que l'HMAC fourni correspond à celui calculé.
     * Utilise une comparaison en temps constant pour éviter les timing attacks.
     *
     * @param providedBase64 HMAC reçu du client, encodé en Base64
     * @param key            la clé secrète (mot de passe en clair)
     * @param message        le message signé (email:nonce:timestamp)
     * @return true si l'HMAC est valide, false sinon
     */
    public static boolean matchesBase64(String providedBase64, String key, String message) {
        if (providedBase64 == null || providedBase64.isBlank()) {
            return false;
        }

        byte[] provided;
        try {
            provided = Base64.getDecoder().decode(providedBase64);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        byte[] expected = computeBytes(key, message);
        return MessageDigest.isEqual(expected, provided);
    }

    /**
     * Calcule l'HMAC-SHA256 en brut (sans Base64).
     *
     * @param key     la clé secrète
     * @param message le message à signer
     * @return les bytes de l'HMAC
     */
    private static byte[] computeBytes(String key, String message) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Impossible de calculer la preuve HMAC", ex);
        }
    }
}

