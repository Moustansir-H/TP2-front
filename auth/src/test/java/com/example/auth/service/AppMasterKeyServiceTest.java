package com.example.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Tests AppMasterKeyService")
class AppMasterKeyServiceTest {

    @Test
    @DisplayName("Utilise APP_MASTER_KEY quand elle est presente")
    void shouldUseEnvValueWhenPresent() {
        String value = AppMasterKeyService.resolveRequiredMasterKey(
                () -> "env-secret-key",
                () -> "property-secret-key"
        );

        assertEquals("env-secret-key", value);
    }

    @Test
    @DisplayName("Fallback sur propriete JVM si variable d'environnement absente")
    void shouldFallbackToPropertyWhenEnvIsMissing() {
        String value = AppMasterKeyService.resolveRequiredMasterKey(
                () -> null,
                () -> "property-secret-key"
        );

        assertEquals("property-secret-key", value);
    }

    @Test
    @DisplayName("Refuse si aucune Master Key n'est fournie")
    void shouldThrowWhenNoKeyProvided() {
        assertThrows(IllegalStateException.class, () ->
                AppMasterKeyService.resolveRequiredMasterKey(() -> "  ", () -> null)
        );
    }
}

