package com.example.auth;

import com.example.auth.service.AppMasterKeyService;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        AppMasterKeyService.getRequiredMasterKey();
        Application.launch(HelloApplication.class, args);
    }
}
