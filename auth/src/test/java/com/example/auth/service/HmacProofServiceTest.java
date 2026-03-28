package com.example.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour HmacProofService.
 * Vérifie le calcul et la vérification des preuves HMAC-SHA256.
 */
@DisplayName("Tests du service HmacProofService (HMAC-SHA256)")
class HmacProofServiceTest {

    @Test
    @DisplayName("TC1 : Calcul HMAC avec valeurs valides")
    void testComputeBase64WithValidInputs() {
        String key = "mySecretPassword";
        String message = "user@example.com:nonce123:1234567890";

        String result = HmacProofService.computeBase64(key, message);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(isBase64(result));
    }

    @Test
    @DisplayName("TC2 : Vérification HMAC valide")
    void testMatchesBase64WithValidHmac() {
        String key = "mySecretPassword";
        String message = "user@example.com:nonce123:1234567890";
        String hmac = HmacProofService.computeBase64(key, message);

        boolean result = HmacProofService.matchesBase64(hmac, key, message);

        assertTrue(result);
    }

    @Test
    @DisplayName("TC3 : Rejet HMAC invalide")
    void testMatchesBase64WithInvalidHmac() {
        String key = "mySecretPassword";
        String message = "user@example.com:nonce123:1234567890";
        String wrongHmac = "aW52YWxpZEhtYWM="; // Base64 invalide

        boolean result = HmacProofService.matchesBase64(wrongHmac, key, message);

        assertFalse(result);
    }

    @Test
    @DisplayName("TC4 : Rejet HMAC NULL")
    void testMatchesBase64WithNullHmac() {
        String key = "mySecretPassword";
        String message = "user@example.com:nonce123:1234567890";

        boolean result = HmacProofService.matchesBase64(null, key, message);

        assertFalse(result);
    }

    @Test
    @DisplayName("TC5 : Rejet HMAC vide")
    void testMatchesBase64WithEmptyHmac() {
        String key = "mySecretPassword";
        String message = "user@example.com:nonce123:1234567890";

        boolean result = HmacProofService.matchesBase64("", key, message);

        assertFalse(result);
    }

    @Test
    @DisplayName("TC6 : Sensibilité à la clé (clés différentes)")
    void testHmacSensitivityToDifferentKeys() {
        String message = "user@example.com:nonce123:1234567890";
        String key1 = "password1";
        String key2 = "password2";

        String hmac1 = HmacProofService.computeBase64(key1, message);
        String hmac2 = HmacProofService.computeBase64(key2, message);

        assertNotEquals(hmac1, hmac2);
    }

    @Test
    @DisplayName("TC7 : Sensibilité au message (messages différents)")
    void testHmacSensitivityToDifferentMessages() {
        String key = "mySecretPassword";
        String message1 = "user@example.com:nonce123:1234567890";
        String message2 = "user@example.com:nonce124:1234567890";

        String hmac1 = HmacProofService.computeBase64(key, message1);
        String hmac2 = HmacProofService.computeBase64(key, message2);

        assertNotEquals(hmac1, hmac2);
    }

    @Test
    @DisplayName("TC8 : Rejet HMAC Base64 invalide")
    void testMatchesBase64WithInvalidBase64() {
        String key = "mySecretPassword";
        String message = "user@example.com:nonce123:1234567890";
        String invalidBase64 = "!!!invalid_base64!!!";

        boolean result = HmacProofService.matchesBase64(invalidBase64, key, message);

        assertFalse(result);
    }

    @Test
    @DisplayName("TC9 : HMAC déterministe (même entrée produit même sortie)")
    void testHmacDeterminism() {
        String key = "mySecretPassword";
        String message = "user@example.com:nonce123:1234567890";

        String hmac1 = HmacProofService.computeBase64(key, message);
        String hmac2 = HmacProofService.computeBase64(key, message);

        assertEquals(hmac1, hmac2);
    }

    @Test
    @DisplayName("TC10 : Vérification avec majuscules et minuscules différentes")
    void testHmacCaseSensitivity() {
        String key = "mySecretPassword";
        String message1 = "user@example.com:nonce123:1234567890";
        String message2 = "USER@EXAMPLE.COM:nonce123:1234567890";

        String hmac1 = HmacProofService.computeBase64(key, message1);
        String hmac2 = HmacProofService.computeBase64(key, message2);

        assertNotEquals(hmac1, hmac2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "x", "a:b:c", "email@domain.com:abc123:1609459200"})
    @DisplayName("TC11-15 : Calcul HMAC avec divers messages")
    void testComputeBase64WithVariousMessages(String message) {
        String key = "secretKey";

        String result = HmacProofService.computeBase64(key, message);

        assertNotNull(result);
        assertTrue(isBase64(result));
    }

    // Helper pour vérifier si une chaîne est du Base64 valide
    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

