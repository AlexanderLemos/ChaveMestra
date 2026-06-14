package com.chavemestra.ui;

import com.chavemestra.controller.VaultController;
import com.chavemestra.model.AuditEvent;
import com.chavemestra.model.VaultFile;
import com.chavemestra.security.SessionManager;
import com.chavemestra.utils.FileUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class DashboardScreen {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final VaultController vaultController;
    private final SessionManager sessionManager;
    private final Runnable onLockRequested;
    private final LocaleManager locale = LocaleManager.getInstance();

    private BorderPane root;
    private ListView<VaultFile> fileListView;
    private ObservableList<VaultFile> fileList;
    private Label fileCountLabel;
    private Label statusLabel;
    private VBox auditPanel;
    private ListView<AuditEvent> auditListView;
    private boolean auditVisible = false;
    private Button openButton;
    private Button deleteButton;
    private Button addButton;
    private Button refreshButton;
    private Button lockButton;
    private Button auditButton;
    private Button passwordGenButton;
    private Button langButton;
    private Label kdfLabel;
    private Label auditTitle;
    private Label emptyTitle;
    private Label emptySubtitle;

    public DashboardScreen(VaultController vaultController, SessionManager sessionManager,
                           Runnable onLockRequested) {
        this.vaultController = vaultController;
        this.sessionManager = sessionManager;
        this.onLockRequested = onLockRequested;
    }

    public Scene createScene() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");

        root.setTop(buildHeader());
        root.setCenter(buildMainContent());
        root.setBottom(buildStatusBar());

        auditPanel = buildAuditPanel();
        auditPanel.setManaged(false);
        auditPanel.setVisible(false);

        Scene scene = new Scene(root, 1100, 750);
        scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        scene.setFill(Color.web("#0d1117"));

        scene.setOnMouseMoved(e -> sessionManager.refreshActivity());
        scene.setOnKeyPressed(e -> sessionManager.refreshActivity());

        setupDragAndDrop();
        refreshFileList();

        Platform.runLater(this::playEntryAnimation);

        return scene;
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        header.setPadding(new Insets(12, 24, 12, 24));

        Label icon = new Label("\uD83D\uDD12");
        icon.setStyle("-fx-font-size: 24px;");

        Label title = new Label(locale.get("app.name"));
        title.getStyleClass().addAll("label", "heading");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        fileCountLabel = new Label("0 " + locale.get("dash.files"));
        fileCountLabel.getStyleClass().addAll("label", "text-secondary");

        auditButton = new Button("\uD83D\uDCCB");
        auditButton.getStyleClass().addAll("button", "icon-button");
        auditButton.setTooltip(new Tooltip(locale.get("dash.tooltip.audit")));
        auditButton.setOnAction(e -> toggleAuditPanel());

        passwordGenButton = new Button("\uD83D\uDD11");
        passwordGenButton.getStyleClass().addAll("button", "icon-button");
        passwordGenButton.setTooltip(new Tooltip(locale.get("dash.tooltip.passgen")));
        passwordGenButton.setOnAction(e -> showPasswordGenerator());

        Label globeLabel = new Label("🌐");
        globeLabel.setStyle("-fx-font-size: 15px;");

        langButton = new Button(locale.getLocaleDisplayCode());
        langButton.getStyleClass().addAll("button", "ghost");
        langButton.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 10; " +
                "-fx-border-color: #30363d; -fx-border-radius: 5; -fx-background-radius: 5;");
        langButton.setTooltip(new Tooltip(locale.get("lang.tooltip")));
        langButton.setOnAction(e -> {
            locale.toggleLocale();
            refreshAllTexts();
        });

        HBox langBox = new HBox(3, globeLabel, langButton);
        langBox.setAlignment(Pos.CENTER);

        lockButton = new Button(locale.get("dash.lock"));
        lockButton.getStyleClass().addAll("button", "danger");
        lockButton.setOnAction(e -> onLockRequested.run());

        header.getChildren().addAll(icon, title, spacer, fileCountLabel,
                auditButton, passwordGenButton, langBox, lockButton);

        return header;
    }

    private VBox buildMainContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setAlignment(Pos.TOP_CENTER);

        HBox toolbar = buildToolbar();

        fileList = FXCollections.observableArrayList();
        fileListView = new ListView<>(fileList);
        fileListView.setCellFactory(param -> new VaultFileCell());
        fileListView.setPlaceholder(buildEmptyState());
        fileListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> updateActionButtons(newVal));
        VBox.setVgrow(fileListView, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, fileListView);

        return content;
    }

    private HBox buildToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        addButton = new Button(locale.get("dash.add"));
        addButton.getStyleClass().addAll("button", "primary");
        addButton.setOnAction(e -> handleAddFile());

        openButton = new Button(locale.get("dash.open"));
        openButton.getStyleClass().addAll("button", "accent");
        openButton.setDisable(true);
        openButton.setOnAction(e -> handleOpenFile());

        deleteButton = new Button(locale.get("dash.delete"));
        deleteButton.getStyleClass().addAll("button", "danger");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> handleDeleteFile());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshButton = new Button(locale.get("dash.refresh"));
        refreshButton.getStyleClass().add("button");
        refreshButton.setOnAction(e -> refreshFileList());

        toolbar.getChildren().addAll(addButton, openButton, deleteButton, spacer, refreshButton);

        return toolbar;
    }

    private VBox buildEmptyState() {
        VBox emptyState = new VBox(16);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));

        Label emptyIcon = new Label("\uD83D\uDD10");
        emptyIcon.setStyle("-fx-font-size: 64px; -fx-opacity: 0.4;");

        emptyTitle = new Label(locale.get("dash.empty.title"));
        emptyTitle.getStyleClass().addAll("label", "heading");
        emptyTitle.setStyle("-fx-opacity: 0.6;");

        emptySubtitle = new Label(locale.get("dash.empty.subtitle"));
        emptySubtitle.getStyleClass().addAll("label", "text-secondary");
        emptySubtitle.setWrapText(true);
        emptySubtitle.setMaxWidth(400);
        emptySubtitle.setAlignment(Pos.CENTER);

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptySubtitle);
        return emptyState;
    }

    private HBox buildStatusBar() {
        HBox statusBar = new HBox(16);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        Label encryptionLabel = new Label(locale.get("dash.statusbar.encryption"));
        encryptionLabel.getStyleClass().addAll("label", "text-accent");
        encryptionLabel.setStyle("-fx-font-size: 11px;");

        kdfLabel = new Label(locale.get("dash.statusbar.kdf"));
        kdfLabel.getStyleClass().addAll("label", "text-secondary");
        kdfLabel.setStyle("-fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel = new Label(locale.get("dash.status.ready"));
        statusLabel.getStyleClass().addAll("label", "text-success");
        statusLabel.setStyle("-fx-font-size: 11px;");

        statusBar.getChildren().addAll(encryptionLabel, kdfLabel, spacer, statusLabel);

        return statusBar;
    }

    private VBox buildAuditPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("sidebar");
        panel.setPrefWidth(360);
        panel.setPadding(new Insets(16));

        HBox panelHeader = new HBox(8);
        panelHeader.setAlignment(Pos.CENTER_LEFT);

        auditTitle = new Label(locale.get("dash.audit.title"));
        auditTitle.getStyleClass().addAll("label", "heading");
        auditTitle.setStyle("-fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("button", "ghost");
        closeBtn.setStyle("-fx-font-size: 16px;");
        closeBtn.setOnAction(e -> toggleAuditPanel());

        panelHeader.getChildren().addAll(auditTitle, spacer, closeBtn);

        auditListView = new ListView<>();
        auditListView.setCellFactory(param -> new AuditEventCell());
        VBox.setVgrow(auditListView, Priority.ALWAYS);

        panel.getChildren().addAll(panelHeader, auditListView);

        return panel;
    }

    private void refreshAllTexts() {
        langButton.setText(locale.getLocaleDisplayCode());
        lockButton.setText(locale.get("dash.lock"));
        addButton.setText(locale.get("dash.add"));
        openButton.setText(locale.get("dash.open"));
        deleteButton.setText(locale.get("dash.delete"));
        refreshButton.setText(locale.get("dash.refresh"));
        auditButton.setTooltip(new Tooltip(locale.get("dash.tooltip.audit")));
        passwordGenButton.setTooltip(new Tooltip(locale.get("dash.tooltip.passgen")));
        kdfLabel.setText(locale.get("dash.statusbar.kdf"));
        statusLabel.setText(locale.get("dash.status.ready"));
        updateFileCount();

        if (emptyTitle != null) {
            emptyTitle.setText(locale.get("dash.empty.title"));
        }
        if (emptySubtitle != null) {
            emptySubtitle.setText(locale.get("dash.empty.subtitle"));
        }
        if (auditTitle != null) {
            auditTitle.setText(locale.get("dash.audit.title"));
        }

        fileListView.refresh();

        if (auditVisible) {
            auditListView.refresh();
        }
    }

    private void toggleAuditPanel() {
        auditVisible = !auditVisible;
        if (auditVisible) {
            refreshAuditLog();
            auditPanel.setManaged(true);
            auditPanel.setVisible(true);
            root.setRight(auditPanel);

            auditPanel.setTranslateX(360);
            TranslateTransition slide = new TranslateTransition(Duration.millis(250), auditPanel);
            slide.setFromX(360);
            slide.setToX(0);
            slide.setInterpolator(Interpolator.EASE_OUT);
            slide.play();
        } else {
            TranslateTransition slide = new TranslateTransition(Duration.millis(200), auditPanel);
            slide.setFromX(0);
            slide.setToX(360);
            slide.setInterpolator(Interpolator.EASE_IN);
            slide.setOnFinished(e -> {
                auditPanel.setManaged(false);
                auditPanel.setVisible(false);
                root.setRight(null);
            });
            slide.play();
        }
    }

    private void setupDragAndDrop() {
        root.setOnDragOver(event -> {
            if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        root.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()) {
                root.setStyle("-fx-background-color: #0d1117; -fx-border-color: #58a6ff; -fx-border-width: 2; -fx-border-style: dashed;");
            }
        });

        root.setOnDragExited(event -> {
            root.setStyle("-fx-background-color: #0d1117;");
        });

        root.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (file.isFile()) {
                        importFileAsync(file.toPath());
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
            root.setStyle("-fx-background-color: #0d1117;");
        });
    }

    private void handleAddFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(locale.get("dash.filechooser.title"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(locale.get("dash.filechooser.filter"), "*.*"));

        Stage stage = (Stage) root.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files != null) {
            for (File file : files) {
                importFileAsync(file.toPath());
            }
        }
    }

    private void importFileAsync(Path filePath) {
        setStatus(locale.get("dash.status.encrypting") + filePath.getFileName() + "...", false);

        Thread importThread = new Thread(() -> {
            try {
                VaultFile vaultFile = vaultController.importFile(filePath);
                Platform.runLater(() -> {
                    fileList.add(vaultFile);
                    updateFileCount();
                    setStatus(locale.get("dash.status.encrypted") + vaultFile.getOriginalName(), false);
                    flashStatus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus(locale.get("dash.status.error") + e.getMessage(), true));
            }
        }, "file-import");
        importThread.setDaemon(true);
        importThread.start();
    }

    private void handleOpenFile() {
        VaultFile selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        setStatus(locale.get("dash.status.decrypting") + selected.getOriginalName() + "...", false);

        Thread openThread = new Thread(() -> {
            try {
                vaultController.openFile(selected);
                Platform.runLater(() -> setStatus(
                        locale.get("dash.status.opened") + selected.getOriginalName()
                                + locale.get("dash.status.autodelete"), false));
            } catch (Exception e) {
                Platform.runLater(() -> setStatus(locale.get("dash.status.error") + e.getMessage(), true));
            }
        }, "file-open");
        openThread.setDaemon(true);
        openThread.start();
    }

    private void handleDeleteFile() {
        VaultFile selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(locale.get("dash.delete.title"));
        confirm.setHeaderText(locale.get("dash.delete.header") + selected.getOriginalName() + "?");
        confirm.setContentText(locale.get("dash.delete.content"));
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("dark-theme.css").toExternalForm());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    vaultController.deleteFile(selected);
                    fileList.remove(selected);
                    updateFileCount();
                    setStatus(locale.get("dash.status.deleted") + selected.getOriginalName(), false);
                } catch (IOException e) {
                    setStatus(locale.get("dash.status.error") + e.getMessage(), true);
                }
            }
        });
    }

    private void showPasswordGenerator() {
        PasswordGeneratorDialog dialog = new PasswordGeneratorDialog(vaultController);
        dialog.show((Stage) root.getScene().getWindow());
    }

    private void refreshFileList() {
        try {
            List<VaultFile> files = vaultController.listFiles();
            fileList.setAll(files);
            updateFileCount();
        } catch (IOException e) {
            setStatus(locale.get("dash.status.load_error") + e.getMessage(), true);
        }
    }

    private void refreshAuditLog() {
        List<AuditEvent> events = vaultController.getAuditLog();
        auditListView.setItems(FXCollections.observableArrayList(events));
        if (!events.isEmpty()) {
            auditListView.scrollTo(events.size() - 1);
        }
    }

    private void updateFileCount() {
        int count = fileList.size();
        String suffix = count != 1 ? locale.get("dash.files") : locale.get("dash.file");
        fileCountLabel.setText(count + suffix);
    }

    private void updateActionButtons(VaultFile selected) {
        boolean hasSelection = selected != null;
        openButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    private void setStatus(String text, boolean isError) {
        statusLabel.setText(text);
        statusLabel.getStyleClass().removeAll("text-success", "text-danger");
        statusLabel.getStyleClass().add(isError ? "text-danger" : "text-success");
    }

    private void flashStatus() {
        FadeTransition flash = new FadeTransition(Duration.millis(200), statusLabel);
        flash.setFromValue(0.3);
        flash.setToValue(1.0);
        flash.setCycleCount(2);
        flash.setAutoReverse(true);
        flash.play();
    }

    private void playEntryAnimation() {
        root.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(400), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private final class VaultFileCell extends ListCell<VaultFile> {
        @Override
        protected void updateItem(VaultFile item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            HBox cell = new HBox(16);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(new Insets(12, 16, 12, 16));
            cell.getStyleClass().add("file-cell");

            Label icon = new Label(FileUtils.getFileIcon(item.getOriginalExtension()));
            icon.setStyle("-fx-font-size: 28px;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label name = new Label(item.getOriginalName());
            name.getStyleClass().add("label");
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            HBox meta = new HBox(12);
            Label size = new Label(FileUtils.formatFileSize(item.getFileSizeBytes()));
            size.getStyleClass().addAll("label", "text-secondary");
            size.setStyle("-fx-font-size: 12px;");

            Label date = new Label(TIME_FORMATTER.format(item.getCreatedAt()));
            date.getStyleClass().addAll("label", "text-secondary");
            date.setStyle("-fx-font-size: 12px;");

            Label encrypted = new Label(locale.get("dash.encrypted"));
            encrypted.getStyleClass().addAll("label", "text-accent");
            encrypted.setStyle("-fx-font-size: 12px;");

            meta.getChildren().addAll(size, date, encrypted);
            info.getChildren().addAll(name, meta);

            Label ext = new Label(item.getOriginalExtension().toUpperCase());
            ext.getStyleClass().addAll("badge", "badge-info");

            cell.getChildren().addAll(icon, info, ext);
            setGraphic(cell);
        }
    }

    private final class AuditEventCell extends ListCell<AuditEvent> {

        private static final DateTimeFormatter AUDIT_TIME =
                DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

        @Override
        protected void updateItem(AuditEvent item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            HBox cell = new HBox(8);
            cell.setPadding(new Insets(6, 8, 6, 8));
            cell.setAlignment(Pos.CENTER_LEFT);

            Label time = new Label(AUDIT_TIME.format(item.timestamp()));
            time.getStyleClass().addAll("label", "monospace", "text-secondary");
            time.setMinWidth(70);

            String badgeClass = switch (item.eventType()) {
                case LOGIN_SUCCESS, ACCOUNT_CREATED, VAULT_UNLOCKED -> "badge-success";
                case LOGIN_FAILED, PANIC_TRIGGERED -> "badge-danger";
                case FILE_ENCRYPTED, FILE_DECRYPTED, FILE_OPENED, PASSWORD_GENERATED -> "badge-info";
                case FILE_DELETED, VAULT_LOCKED -> "badge-warning";
            };

            String typeKey = "audit." + item.eventType().name().toLowerCase();
            Label type = new Label(locale.get(typeKey));
            type.getStyleClass().addAll("badge", badgeClass);
            type.setMinWidth(100);

            Label details = new Label(item.details());
            details.getStyleClass().addAll("label", "monospace", "text-secondary");
            details.setStyle("-fx-font-size: 11px;");
            HBox.setHgrow(details, Priority.ALWAYS);

            cell.getChildren().addAll(time, type, details);
            setGraphic(cell);
        }
    }
}
