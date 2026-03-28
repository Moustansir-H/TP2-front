package com.example.auth.controller;

import com.example.auth.model.ApiResult;
import com.example.auth.service.AuthApiClient;
import com.example.auth.service.ClientValidationService;
import com.example.auth.service.TokenPersistenceService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AuthController {

    @FXML
    private TextField serverUrlField;

    @FXML
    private TextField registerPathField;

    @FXML
    private TextField loginPathField;

    @FXML
    private CheckBox ssoModeCheckBox;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private PasswordField registerPasswordConfirmField;

    @FXML
    private Label registerStatusLabel;

    @FXML
    private ProgressBar strengthBar;

    @FXML
    private Label strengthLabel;

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private Label loginStatusLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    private AuthApiClient apiClient;

    @FXML
    public void initialize() {
        serverUrlField.setText("http://localhost:8080");
        registerPathField.setText("/api/auth/register");
        loginPathField.setText("/api/auth/login");
        ssoModeCheckBox.setSelected(true);

        rebuildApiClient();

        String savedToken = TokenPersistenceService.getToken();
        if (savedToken != null && !savedToken.isBlank()) {
            apiClient.setAccessToken(savedToken);
        }

        registerPasswordField.textProperty().addListener((obs, oldValue, newValue) -> updateStrength(newValue));

        // Reconfigure le client dès qu'un champ réseau est modifié.
        serverUrlField.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (Boolean.FALSE.equals(newFocused)) {
                rebuildApiClient();
            }
        });
        registerPathField.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (Boolean.FALSE.equals(newFocused)) {
                rebuildApiClient();
            }
        });
        loginPathField.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (Boolean.FALSE.equals(newFocused)) {
                rebuildApiClient();
            }
        });
        ssoModeCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> rebuildApiClient());

        updateStrength("");
    }

    @FXML
    private void onRegister() {
        clearRegisterStatus();

        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String confirm = registerPasswordConfirmField.getText();

        if (!ClientValidationService.isEmailValid(email)) {
            setRegisterStatus("Email invalide", false);
            return;
        }

        if (!ClientValidationService.isPasswordPolicyValid(password)) {
            setRegisterStatus("Mot de passe invalide: 12+ caractères, majuscule, minuscule, chiffre, caractère spécial requis", false);
            return;
        }

        if (confirm == null || !confirm.equals(password)) {
            setRegisterStatus("La confirmation du mot de passe ne correspond pas", false);
            return;
        }

        runAsync(registerButton, () -> apiClient.register(email, password), result -> {
            setRegisterStatus(result.message(), result.success());
            if (result.success()) {
                registerPasswordField.clear();
                registerPasswordConfirmField.clear();
                updateStrength("");
            }
        });
    }

    @FXML
    private void onLogin() {
        clearLoginStatus();

        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();

        if (!ClientValidationService.isEmailValid(email)) {
            setLoginStatus("Email invalide", false);
            return;
        }

        if (password == null || password.isBlank()) {
            setLoginStatus("Mot de passe obligatoire", false);
            return;
        }

        runAsync(loginButton, () -> apiClient.login(email, password), result -> {
            setLoginStatus(result.message(), result.success());
            if (result.success()) {
                String token = apiClient.getAccessToken();
                if (token != null) {
                    TokenPersistenceService.saveToken(token);
                }
                loginPasswordField.clear();
            }
        });
    }

    private void updateStrength(String password) {
        ClientValidationService.StrengthLevel level = ClientValidationService.getStrengthLevel(password);
        strengthLabel.setText(ClientValidationService.strengthLabel(level));

        strengthBar.getStyleClass().removeAll("strength-red", "strength-orange", "strength-green");

        switch (level) {
            case RED -> {
                strengthBar.setProgress(0.33);
                strengthBar.getStyleClass().add("strength-red");
            }
            case ORANGE -> {
                strengthBar.setProgress(0.66);
                strengthBar.getStyleClass().add("strength-orange");
            }
            case GREEN -> {
                strengthBar.setProgress(1.0);
                strengthBar.getStyleClass().add("strength-green");
            }
        }
    }

    private void rebuildApiClient() {
        String previousToken = apiClient == null ? TokenPersistenceService.getToken() : apiClient.getAccessToken();
        apiClient = new AuthApiClient(
                serverUrlField.getText(),
                registerPathField.getText(),
                loginPathField.getText(),
                ssoModeCheckBox.isSelected()
        );
        if (previousToken != null && !previousToken.isBlank()) {
            apiClient.setAccessToken(previousToken);
        }
    }

    private void runAsync(Button button, ApiCall call, ApiResultHandler handler) {
        button.setDisable(true);

        Task<ApiResult> task = new Task<>() {
            @Override
            protected ApiResult call() {
                return call.execute();
            }
        };

        task.setOnSucceeded(event -> {
            button.setDisable(false);
            handler.handle(task.getValue());
        });

        task.setOnFailed(event -> {
            button.setDisable(false);
            Throwable ex = task.getException();
            String message = ex == null ? "Erreur inattendue" : ex.getMessage();
            Platform.runLater(() -> {
                if (button == registerButton) {
                    setRegisterStatus(message, false);
                } else {
                    setLoginStatus(message, false);
                }
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void setRegisterStatus(String message, boolean success) {
        registerStatusLabel.setText(message);
        registerStatusLabel.setStyle(success ? "-fx-text-fill: #1b5e20;" : "-fx-text-fill: #b71c1c;");
    }

    private void setLoginStatus(String message, boolean success) {
        loginStatusLabel.setText(message);
        loginStatusLabel.setStyle(success ? "-fx-text-fill: #1b5e20;" : "-fx-text-fill: #b71c1c;");
    }

    private void clearRegisterStatus() {
        registerStatusLabel.setText("");
    }

    private void clearLoginStatus() {
        loginStatusLabel.setText("");
    }

    @FunctionalInterface
    private interface ApiCall {
        ApiResult execute();
    }

    @FunctionalInterface
    private interface ApiResultHandler {
        void handle(ApiResult result);
    }
}
