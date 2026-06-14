package com.chavemestra.ui;

import com.chavemestra.controller.VaultController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class PasswordGeneratorDialog {

    private final VaultController vaultController;
    private final LocaleManager locale = LocaleManager.getInstance();

    private TextField passwordDisplay;
    private Slider lengthSlider;
    private Label lengthLabel;
    private CheckBox uppercaseCheck;
    private CheckBox lowercaseCheck;
    private CheckBox digitsCheck;
    private CheckBox symbolsCheck;
    private ProgressBar strengthBar;
    private Label strengthLabel;
    private Label copiedLabel;

    public PasswordGeneratorDialog(VaultController vaultController) {
        this.vaultController = vaultController;
    }

    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(locale.get("passgen.title"));

        VBox root = new VBox(20);
        root.getStyleClass().add("card-elevated");
        root.setPadding(new Insets(32));
        root.setMaxWidth(480);
        root.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-radius: 16; -fx-background-radius: 16;");

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label titleIcon = new Label("\uD83D\uDD11");
        titleIcon.setStyle("-fx-font-size: 22px;");

        Label title = new Label("  " + locale.get("passgen.title"));
        title.getStyleClass().addAll("label", "heading");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("button", "ghost");
        closeBtn.setStyle("-fx-font-size: 18px;");
        closeBtn.setOnAction(e -> dialog.close());

        titleBar.getChildren().addAll(titleIcon, title, spacer, closeBtn);

        passwordDisplay = new TextField();
        passwordDisplay.setEditable(false);
        passwordDisplay.setStyle("-fx-font-family: 'Consolas', 'Cascadia Code', monospace; -fx-font-size: 16px; " +
                "-fx-background-color: #0d1117; -fx-text-fill: #58a6ff; -fx-border-color: #30363d; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 14 16;");

        HBox passwordActions = new HBox(8);
        passwordActions.setAlignment(Pos.CENTER_RIGHT);

        copiedLabel = new Label(locale.get("passgen.copied"));
        copiedLabel.getStyleClass().addAll("label", "text-success");
        copiedLabel.setStyle("-fx-font-size: 12px;");
        copiedLabel.setVisible(false);

        Button copyBtn = new Button(locale.get("passgen.copy"));
        copyBtn.getStyleClass().addAll("button", "accent");
        copyBtn.setOnAction(e -> copyToClipboard());

        Button regenerateBtn = new Button(locale.get("passgen.regenerate"));
        regenerateBtn.getStyleClass().add("button");
        regenerateBtn.setOnAction(e -> generatePassword());

        passwordActions.getChildren().addAll(copiedLabel, copyBtn, regenerateBtn);

        HBox lengthBox = new HBox(12);
        lengthBox.setAlignment(Pos.CENTER_LEFT);

        Label lengthTitle = new Label(locale.get("passgen.length"));
        lengthTitle.getStyleClass().add("label");
        lengthTitle.setMinWidth(60);

        lengthSlider = new Slider(8, 128, 20);
        lengthSlider.setShowTickMarks(false);
        HBox.setHgrow(lengthSlider, Priority.ALWAYS);
        lengthSlider.valueProperty().addListener((obs, old, val) -> {
            lengthLabel.setText(String.valueOf(val.intValue()));
            generatePassword();
        });

        lengthLabel = new Label("20");
        lengthLabel.getStyleClass().addAll("label", "text-accent");
        lengthLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 30;");

        lengthBox.getChildren().addAll(lengthTitle, lengthSlider, lengthLabel);

        HBox optionsBox = new HBox(16);
        optionsBox.setAlignment(Pos.CENTER_LEFT);

        uppercaseCheck = new CheckBox("A-Z");
        uppercaseCheck.setSelected(true);
        uppercaseCheck.selectedProperty().addListener((obs, old, val) -> generatePassword());

        lowercaseCheck = new CheckBox("a-z");
        lowercaseCheck.setSelected(true);
        lowercaseCheck.selectedProperty().addListener((obs, old, val) -> generatePassword());

        digitsCheck = new CheckBox("0-9");
        digitsCheck.setSelected(true);
        digitsCheck.selectedProperty().addListener((obs, old, val) -> generatePassword());

        symbolsCheck = new CheckBox("!@#$");
        symbolsCheck.setSelected(true);
        symbolsCheck.selectedProperty().addListener((obs, old, val) -> generatePassword());

        optionsBox.getChildren().addAll(uppercaseCheck, lowercaseCheck, digitsCheck, symbolsCheck);

        VBox strengthBox = new VBox(6);

        HBox strengthHeader = new HBox();
        strengthHeader.setAlignment(Pos.CENTER_LEFT);

        Label strengthTitle = new Label(locale.get("passgen.strength"));
        strengthTitle.getStyleClass().addAll("label", "text-secondary");
        strengthTitle.setStyle("-fx-font-size: 12px;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        strengthLabel = new Label(locale.get("passgen.strength.strong"));
        strengthLabel.getStyleClass().addAll("label");
        strengthLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        strengthHeader.getChildren().addAll(strengthTitle, spacer2, strengthLabel);

        strengthBar = new ProgressBar(0.8);
        strengthBar.setMaxWidth(Double.MAX_VALUE);
        strengthBar.setPrefHeight(6);

        strengthBox.getChildren().addAll(strengthHeader, strengthBar);

        root.getChildren().addAll(titleBar, passwordDisplay, passwordActions,
                new Separator(), lengthBox, optionsBox, new Separator(), strengthBox);

        Scene scene = new Scene(root, 480, 420);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());

        dialog.setScene(scene);
        generatePassword();
        dialog.showAndWait();
    }

    private void generatePassword() {
        int length = (int) lengthSlider.getValue();
        String password = vaultController.generatePassword(length,
                uppercaseCheck.isSelected(),
                lowercaseCheck.isSelected(),
                digitsCheck.isSelected(),
                symbolsCheck.isSelected());
        passwordDisplay.setText(password);
        updateStrengthIndicator(password);
        copiedLabel.setVisible(false);
    }

    private void copyToClipboard() {
        ClipboardContent content = new ClipboardContent();
        content.putString(passwordDisplay.getText());
        Clipboard.getSystemClipboard().setContent(content);
        copiedLabel.setVisible(true);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(2));
        pause.setOnFinished(e -> copiedLabel.setVisible(false));
        pause.play();
    }

    private void updateStrengthIndicator(String password) {
        double score = calculateStrength(password);
        strengthBar.setProgress(score);

        strengthBar.getStyleClass().removeAll(
                "strength-weak", "strength-fair", "strength-good", "strength-strong");

        if (score < 0.3) {
            strengthLabel.setText(locale.get("passgen.strength.weak"));
            strengthLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f85149;");
            strengthBar.getStyleClass().add("strength-weak");
        } else if (score < 0.55) {
            strengthLabel.setText(locale.get("passgen.strength.fair"));
            strengthLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #d29922;");
            strengthBar.getStyleClass().add("strength-fair");
        } else if (score < 0.8) {
            strengthLabel.setText(locale.get("passgen.strength.good"));
            strengthLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");
            strengthBar.getStyleClass().add("strength-good");
        } else {
            strengthLabel.setText(locale.get("passgen.strength.strong"));
            strengthLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #3fb950;");
            strengthBar.getStyleClass().add("strength-strong");
        }
    }

    private double calculateStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        double score = 0;
        int length = password.length();

        score += Math.min(0.3, length / 50.0);

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSymbol = true;
        }

        int charsetSize = 0;
        if (hasUpper) charsetSize += 26;
        if (hasLower) charsetSize += 26;
        if (hasDigit) charsetSize += 10;
        if (hasSymbol) charsetSize += 30;

        score += Math.min(0.3, charsetSize / 100.0);

        double entropy = length * (Math.log(Math.max(charsetSize, 1)) / Math.log(2));
        score += Math.min(0.4, entropy / 256.0);

        return Math.min(1.0, score);
    }
}
