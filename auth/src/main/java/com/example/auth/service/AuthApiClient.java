package com.example.auth.service;

import com.example.auth.model.ApiResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client API pour communiquer avec le serveur d'authentification.
 * Compatible TP3 (SSO HMAC) avec fallback login classique si nécessaire.
 */
public class AuthApiClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String registerPath;
    private final String loginPath;
    private final boolean ssoEnabled;

    private String accessToken;

    public AuthApiClient(String baseUrl) {
        this(baseUrl, "/api/auth/register", "/api/auth/login", true);
    }

    public AuthApiClient(String baseUrl, String registerPath, String loginPath, boolean ssoEnabled) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.registerPath = normalizePath(registerPath, "/api/auth/register");
        this.loginPath = normalizePath(loginPath, "/api/auth/login");
        this.ssoEnabled = ssoEnabled;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.accessToken = null;
    }

    public ApiResult register(String email, String password) {
        String body = "{"
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"password\":\"" + escapeJson(password) + "\""
                + "}";
        return post(registerPath, body, false);
    }

    public ApiResult login(String email, String password) {
        String body = ssoEnabled ? buildSsoBody(email, password) : buildClassicBody(email, password);

        ApiResult result = post(loginPath, body, false);

        if (result.success()) {
            try {
                extractAndStoreToken(result.rawBody());
            } catch (Exception e) {
                return new ApiResult(false, result.statusCode(), "Connexion OK mais token invalide en réponse", result.rawBody());
            }
        }

        return result;
    }

    public ApiResult getWithToken(String path) {
        if (accessToken == null || accessToken.isBlank()) {
            return new ApiResult(false, 401, "Token manquant. Veuillez vous connecter.", "");
        }
        return getRequest(path);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public void clearAccessToken() {
        this.accessToken = null;
    }

    private String buildSsoBody(String email, String password) {
        String nonce = NonceGenerator.generateNonce();
        long timestamp = System.currentTimeMillis() / 1000;
        String message = email + ":" + nonce + ":" + timestamp;
        String hmac = HmacProofService.computeBase64(password, message);

        return "{"
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"nonce\":\"" + escapeJson(nonce) + "\","
                + "\"timestamp\":" + timestamp + ","
                + "\"hmac\":\"" + escapeJson(hmac) + "\""
                + "}";
    }

    private String buildClassicBody(String email, String password) {
        return "{"
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"password\":\"" + escapeJson(password) + "\""
                + "}";
    }

    private ApiResult post(String path, String jsonBody, boolean includeAuth) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + normalizePath(path, "/")))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json");

        if (includeAuth && accessToken != null && !accessToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return toApiResult(response, "Connexion réussie");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new ApiResult(false, 0, "Requête interrompue", ex.getMessage());
        } catch (IOException ex) {
            return new ApiResult(false, 0, "Impossible de contacter le serveur backend. Vérifie qu'il tourne sur " + baseUrl, ex.getMessage());
        }
    }

    private ApiResult getRequest(String path) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + normalizePath(path, "/")))
                .timeout(Duration.ofSeconds(8));

        if (accessToken != null && !accessToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        HttpRequest request = requestBuilder.GET().build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return toApiResult(response, "Requête réussie");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new ApiResult(false, 0, "Requête interrompue", ex.getMessage());
        } catch (IOException ex) {
            return new ApiResult(false, 0, "Impossible de contacter le serveur", ex.getMessage());
        }
    }

    private ApiResult toApiResult(HttpResponse<String> response, String defaultSuccessMessage) {
        int status = response.statusCode();
        String body = response.body() == null ? "" : response.body();

        if (status >= 200 && status < 300) {
            String message = extractMessage(body);
            if (message == null || message.isBlank()) {
                message = status == 201 ? "Inscription réussie" : defaultSuccessMessage;
            }
            return new ApiResult(true, status, message, body);
        }

        String backendMessage = extractMessage(body);
        if (backendMessage == null || backendMessage.isBlank()) {
            backendMessage = "Erreur serveur (" + status + ")";
        }
        return new ApiResult(false, status, backendMessage, body);
    }

    private void extractAndStoreToken(String jsonBody) {
        String[] tokenKeys = new String[]{"accessToken", "token", "jwt"};
        for (String tokenKey : tokenKeys) {
            String token = extractStringField(jsonBody, tokenKey);
            if (token != null && !token.isBlank()) {
                this.accessToken = token;
                return;
            }
        }
        throw new RuntimeException("Token non trouvé dans la réponse");
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8080";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String normalizePath(String path, String defaultPath) {
        String value = path;
        if (value == null || value.isBlank()) {
            value = defaultPath;
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String extractMessage(String jsonBody) {
        return extractStringField(jsonBody, "message");
    }

    private static String extractStringField(String jsonBody, String fieldName) {
        if (jsonBody == null || jsonBody.isBlank()) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(jsonBody);
        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
        }
        return null;
    }
}
