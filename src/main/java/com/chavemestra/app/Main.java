package com.chavemestra.app;

import com.chavemestra.controller.LoginController;
import com.chavemestra.controller.VaultController;
import com.chavemestra.crypto.AESService;
import com.chavemestra.crypto.HashService;
import com.chavemestra.crypto.KeyDerivationService;
import com.chavemestra.crypto.PasswordVerifier;
import com.chavemestra.security.AttemptLimiter;
import com.chavemestra.security.PanicMode;
import com.chavemestra.security.SessionManager;
import com.chavemestra.storage.AuditLogStorage;
import com.chavemestra.storage.FileStorage;
import com.chavemestra.storage.MetadataStorage;
import com.chavemestra.ui.DashboardScreen;
import com.chavemestra.ui.LoginScreen;
import com.chavemestra.utils.FileUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public final class Main extends Application {

    private KeyDerivationService keyDerivationService;
    private HashService hashService;
    private AESService aesService;
    private PasswordVerifier passwordVerifier;
    private MetadataStorage metadataStorage;
    private FileStorage fileStorage;
    private AuditLogStorage auditLogStorage;
    private AttemptLimiter attemptLimiter;
    private SessionManager sessionManager;
    private PanicMode panicMode;
    private LoginController loginController;
    private VaultController vaultController;

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            FileUtils.createVaultDirectories();
        } catch (IOException e) {
            System.err.println("Failed to create vault directories: " + e.getMessage());
            Platform.exit();
            return;
        }

        initializeServices();

        stage.setTitle("ChaveMestra");
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        showLoginScreen();

        stage.setOnCloseRequest(e -> shutdown());
        stage.show();
    }

    private void initializeServices() {
        keyDerivationService = new KeyDerivationService();
        hashService = new HashService(keyDerivationService);
        aesService = new AESService();
        passwordVerifier = new PasswordVerifier(hashService);

        metadataStorage = new MetadataStorage();
        auditLogStorage = new AuditLogStorage();

        fileStorage = new FileStorage(aesService, keyDerivationService);

        attemptLimiter = new AttemptLimiter();
        sessionManager = new SessionManager();
        panicMode = new PanicMode(fileStorage, metadataStorage, auditLogStorage);

        loginController = new LoginController(metadataStorage, passwordVerifier,
                attemptLimiter, sessionManager, panicMode, auditLogStorage);

        vaultController = new VaultController(fileStorage, metadataStorage,
                auditLogStorage, sessionManager);
    }

    private void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(loginController, this::onLoginResult);
        Scene scene = loginScreen.createScene();
        primaryStage.setScene(scene);
    }

    private void onLoginResult(char[] password) {
        if (password == null) {
            showLoginScreen();
            return;
        }
        Arrays.fill(password, '\0');
        showDashboard();
    }

    private void showDashboard() {
        DashboardScreen dashboard = new DashboardScreen(
                vaultController, sessionManager, this::handleLock);
        Scene scene = dashboard.createScene();
        primaryStage.setScene(scene);
    }

    private void handleLock() {
        loginController.logout();
        showLoginScreen();
    }

    private void shutdown() {
        if (vaultController != null) {
            vaultController.shutdown();
        }
        if (sessionManager != null) {
            sessionManager.destroySession();
        }
        Platform.exit();
    }

    @Override
    public void stop() {
        shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
