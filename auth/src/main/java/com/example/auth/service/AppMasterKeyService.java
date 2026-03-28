package com.example.auth.service;

import java.util.function.Supplier;

/**
 * Valide la presence de la Master Key imposee par le TP4.
 *
 * La cle est lue en priorite depuis APP_MASTER_KEY (variable d'environnement),
 * puis depuis la propriete JVM app.master.key pour simplifier les tests locaux.
 */
public final class AppMasterKeyService {

    public static final String APP_MASTER_KEY_ENV = "APP_MASTER_KEY";
    public static final String APP_MASTER_KEY_PROPERTY = "app.master.key";

    private AppMasterKeyService() {
    }

    public static String getRequiredMasterKey() {
        return resolveRequiredMasterKey(
                () -> System.getenv(APP_MASTER_KEY_ENV),
                () -> System.getProperty(APP_MASTER_KEY_PROPERTY)
        );
    }

    static String resolveRequiredMasterKey(Supplier<String> envSupplier, Supplier<String> propertySupplier) {
        String key = safeTrim(envSupplier.get());
        if (key == null) {
            key = safeTrim(propertySupplier.get());
        }

        if (key == null) {
            throw new IllegalStateException(
                    "APP_MASTER_KEY est obligatoire. Definis APP_MASTER_KEY dans l'environnement avant de demarrer l'application."
            );
        }

        return key;
    }

    private static String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

