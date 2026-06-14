package com.chavemestra.ui;

import com.chavemestra.controller.LoginController;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.function.Consumer;

public final class LoginScreen {

    private final LoginController loginController;
    private final Consumer<char[]> onLoginSuccess;
    private final LocaleManager locale = LocaleManager.getInstance();

    private VBox root;
    private PasswordField passwordField;
    private PasswordField confirmField;
    private Label messageLabel;
    private Label attemptsLabel;
    private Label lockoutLabel;
    private Label subtitleLabel;
    private Button actionButton;
    private Button langButton;
    private VBox confirmBox;
    private boolean registrationMode;
    private Timeline lockoutTimer;

    public LoginScreen(LoginController loginController, Consumer<char[]> onLoginSuccess) {
        this.loginController = loginController;
        this.onLoginSuccess = onLoginSuccess;
        this.registrationMode = loginController.isFirstLaunch();
    }

    public Scene createScene() {
        root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(0);
        root.setStyle("-fx-background-color: #0d1117;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(12, 20, 0, 20));

        langButton = new Button(locale.getLocaleDisplayCode());
        langButton.getStyleClass().addAll("button", "ghost");
        langButton.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 14; " +
                "-fx-border-color: #30363d; -fx-border-radius: 6; -fx-background-radius: 6;");
        langButton.setTooltip(new Tooltip(locale.get("lang.tooltip")));
        langButton.setOnAction(e -> {
            locale.toggleLocale();
            refreshTexts();
        });

        Label globeLabel = new Label("🌐 ");
        globeLabel.setStyle("-fx-font-size: 14px;");

        HBox langBox = new HBox(4, globeLabel, langButton);
        langBox.setAlignment(Pos.CENTER_RIGHT);

        topBar.getChildren().add(langBox);

        VBox loginCard = buildLoginCard();
        loginCard.setMaxWidth(420);
        loginCard.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane container = new StackPane(loginCard);
        container.setAlignment(Pos.CENTER);
        VBox.setVgrow(container, Priority.ALWAYS);

        root.getChildren().addAll(topBar, container);

        Scene scene = new Scene(root, 960, 680);
        scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        scene.setFill(Color.web("#0d1117"));

        Platform.runLater(this::playEntryAnimation);

        return scene;
    }

    private VBox buildLoginCard() {
        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card-elevated");
        card.setPadding(new Insets(48, 40, 40, 40));

        Label iconLabel = new Label("\uD83D\uDD12");
        iconLabel.getStyleClass().addAll("label", "brand-icon");

        Label titleLabel = new Label(locale.get("login.title"));
        titleLabel.getStyleClass().addAll("label", "brand-title");

        subtitleLabel = new Label(registrationMode
                ? locale.get("login.subtitle.register")
                : locale.get("login.subtitle.login"));
        subtitleLabel.getStyleClass().addAll("label", "subtitle");
        subtitleLabel.setWrapText(true);

        VBox headerBox = new VBox(8, iconLabel, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        Region divider = new Region();
        divider.getStyleClass().add("divider");
        divider.setMaxWidth(Double.MAX_VALUE);

        passwordField = new PasswordField();
        passwordField.setPromptText(locale.get("login.password.prompt"));
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setOnAction(e -> handleAction());

        confirmField = new PasswordField();
        confirmField.setPromptText(locale.get("login.password.confirm"));
        confirmField.setMaxWidth(Double.MAX_VALUE);
        confirmField.setOnAction(e -> handleAction());

        confirmBox = new VBox(confirmField);
        confirmBox.setManaged(registrationMode);
        confirmBox.setVisible(registrationMode);

        VBox fieldsBox = new VBox(12, passwordField, confirmBox);
        fieldsBox.setMaxWidth(Double.MAX_VALUE);

        actionButton = new Button(registrationMode
                ? locale.get("login.button.create")
                : locale.get("login.button.unlock"));
        actionButton.getStyleClass().addAll("button", registrationMode ? "primary" : "accent");
        actionButton.setMaxWidth(Double.MAX_VALUE);
        actionButton.setPrefHeight(44);
        actionButton.setOnAction(e -> handleAction());

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        attemptsLabel = new Label();
        attemptsLabel.getStyleClass().addAll("label", "text-secondary");
        attemptsLabel.setAlignment(Pos.CENTER);
        attemptsLabel.setMaxWidth(Double.MAX_VALUE);
        attemptsLabel.setVisible(false);
        attemptsLabel.setManaged(false);

        lockoutLabel = new Label();
        lockoutLabel.getStyleClass().addAll("label", "text-warning");
        lockoutLabel.setAlignment(Pos.CENTER);
        lockoutLabel.setMaxWidth(Double.MAX_VALUE);
        lockoutLabel.setVisible(false);
        lockoutLabel.setManaged(false);

        Label versionLabel = new Label(locale.get("app.version"));
        versionLabel.getStyleClass().addAll("label", "text-secondary");
        versionLabel.setStyle("-fx-font-size: 11px;");
        versionLabel.setAlignment(Pos.CENTER);
        versionLabel.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(
                headerBox, divider, fieldsBox, actionButton,
                messageLabel, attemptsLabel, lockoutLabel, versionLabel);

        return card;
    }

    private void refreshTexts() {
        langButton.setText(locale.getLocaleDisplayCode());
        langButton.setTooltip(new Tooltip(locale.get("lang.tooltip")));
        subtitleLabel.setText(registrationMode
                ? locale.get("login.subtitle.register")
                : locale.get("login.subtitle.login"));
        passwordField.setPromptText(locale.get("login.password.prompt"));
        confirmField.setPromptText(locale.get("login.password.confirm"));
        actionButton.setText(registrationMode
                ? locale.get("login.button.create")
                : locale.get("login.button.unlock"));
    }

    private void handleAction() {
        if (registrationMode) {
            handleRegistration();
        } else {
            handleLogin();
        }
    }

    private void handleRegistration() {
        char[] password = getPasswordChars(passwordField);
        char[] confirm = getPasswordChars(confirmField);

        if (password.length == 0) {
            showMessage(locale.get("login.error.empty"), true);
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        LoginController.RegistrationResult result = loginController.register(password, confirm);

        if (result.success()) {
            showMessage(locale.get("login.success.created"), false);
            char[] pw = result.password();
            Runnable onExpired = () -> Platform.runLater(() -> {
                loginController.logout();
                onLoginSuccess.accept(null);
            });
            loginController.login(pw, onExpired);
            Arrays.fill(pw, '\0');

            PauseTransition delay = new PauseTransition(Duration.millis(500));
            delay.setOnFinished(e -> {
                char[] sessionPw = loginController.getSessionManager().getMasterPassword();
                onLoginSuccess.accept(sessionPw);
            });
            delay.play();
        } else {
            showMessage(result.message(), true);
            shakeCard();
        }
    }

    private void handleLogin() {
        char[] password = getPasswordChars(passwordField);

        if (password.length == 0) {
            showMessage(locale.get("login.error.empty"), true);
            return;
        }

        Runnable onExpired = () -> Platform.runLater(() -> {
            loginController.logout();
            onLoginSuccess.accept(null);
        });

        LoginController.LoginResult result = loginController.login(password, onExpired);

        switch (result.status()) {
            case SUCCESS -> {
                showMessage(locale.get("login.success.granted"), false);
                actionButton.setDisable(true);
                PauseTransition delay = new PauseTransition(Duration.millis(300));
                delay.setOnFinished(e -> {
                    char[] sessionPw = loginController.getSessionManager().getMasterPassword();
                    onLoginSuccess.accept(sessionPw);
                });
                delay.play();
            }
            case FAILURE -> {
                showMessage(result.message(), true);
                if (result.remainingAttempts() > 0) {
                    showAttempts(result.remainingAttempts());
                }
                shakeCard();
                passwordField.clear();
                passwordField.requestFocus();
            }
            case LOCKED -> {
                showMessage(result.message(), true);
                startLockoutCountdown(result.lockoutSeconds());
                shakeCard();
                passwordField.clear();
            }
            case PANIC -> {
                showPanicState(result.message());
            }
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.getStyleClass().removeAll("text-danger", "text-success");
        messageLabel.getStyleClass().add(isError ? "text-danger" : "text-success");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(200), messageLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void showAttempts(int remaining) {
        attemptsLabel.setText(remaining + locale.get("login.attempts.remaining"));
        attemptsLabel.setVisible(true);
        attemptsLabel.setManaged(true);
    }

    private void startLockoutCountdown(long seconds) {
        actionButton.setDisable(true);
        passwordField.setDisable(true);
        lockoutLabel.setVisible(true);
        lockoutLabel.setManaged(true);
        attemptsLabel.setVisible(false);
        attemptsLabel.setManaged(false);

        if (lockoutTimer != null) {
            lockoutTimer.stop();
        }

        final long[] remaining = {seconds};
        lockoutTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                lockoutLabel.setVisible(false);
                lockoutLabel.setManaged(false);
                actionButton.setDisable(false);
                passwordField.setDisable(false);
                passwordField.requestFocus();
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
                if (lockoutTimer != null) {
                    lockoutTimer.stop();
                }
            } else {
                lockoutLabel.setText(locale.get("login.lockout.prefix")
                        + remaining[0] + locale.get("login.lockout.suffix"));
            }
        }));
        lockoutTimer.setCycleCount((int) seconds);
        lockoutLabel.setText(locale.get("login.lockout.prefix")
                + seconds + locale.get("login.lockout.suffix"));
        lockoutTimer.play();
    }

    private void showPanicState(String message) {
        passwordField.setDisable(true);
        actionButton.setDisable(true);

        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("text-danger", "text-success");
        messageLabel.getStyleClass().add("text-danger");
        messageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        attemptsLabel.setVisible(false);
        attemptsLabel.setManaged(false);

        root.setStyle("-fx-background-color: #1a0000;");

        FadeTransition flash = new FadeTransition(Duration.millis(500), messageLabel);
        flash.setFromValue(0.3);
        flash.setToValue(1.0);
        flash.setCycleCount(Animation.INDEFINITE);
        flash.setAutoReverse(true);
        flash.play();
    }

    private void shakeCard() {
        if (root.getChildren().size() < 2) return;
        javafx.scene.Node card = ((StackPane) root.getChildren().get(1)).getChildren().get(0);

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), card);
        shake.setFromX(-10);
        shake.setToX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> card.setTranslateX(0));
        shake.play();
    }

    private void playEntryAnimation() {
        if (root.getChildren().size() < 2) return;
        javafx.scene.Node card = ((StackPane) root.getChildren().get(1)).getChildren().get(0);

        card.setOpacity(0);
        card.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(600), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(600), card);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entry = new ParallelTransition(fade, slide);
        entry.play();

        PauseTransition focusDelay = new PauseTransition(Duration.millis(700));
        focusDelay.setOnFinished(e -> passwordField.requestFocus());
        focusDelay.play();
    }

    private char[] getPasswordChars(PasswordField field) {
        String text = field.getText();
        if (text == null || text.isEmpty()) {
            return new char[0];
        }
        return text.toCharArray();
    }
}
