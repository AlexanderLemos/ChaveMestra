package com.chavemestra.ui;

import com.chavemestra.model.AuditEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class AuditLogView extends VBox {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ListView<AuditEvent> listView;
    private final ObservableList<AuditEvent> events;

    public AuditLogView() {
        events = FXCollections.observableArrayList();
        listView = new ListView<>(events);
        listView.setCellFactory(param -> new AuditCell());

        setSpacing(8);
        setPadding(new Insets(12));
        getStyleClass().add("surface");

        Label header = new Label("Security Event Log");
        header.getStyleClass().addAll("label", "heading");
        header.setStyle("-fx-font-size: 16px;");

        VBox.setVgrow(listView, Priority.ALWAYS);
        getChildren().addAll(header, listView);
    }

    public void setEvents(List<AuditEvent> eventList) {
        events.setAll(eventList);
        if (!eventList.isEmpty()) {
            listView.scrollTo(eventList.size() - 1);
        }
    }

    public void addEvent(AuditEvent event) {
        events.add(event);
        listView.scrollTo(events.size() - 1);
    }

    private final class AuditCell extends ListCell<AuditEvent> {
        @Override
        protected void updateItem(AuditEvent item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 8, 4, 8));

            Label timestamp = new Label(TIMESTAMP_FORMAT.format(item.timestamp()));
            timestamp.getStyleClass().addAll("label", "monospace", "text-secondary");
            timestamp.setStyle("-fx-font-size: 11px;");
            timestamp.setMinWidth(130);

            String eventIcon = resolveIcon(item.eventType());
            Label icon = new Label(eventIcon);
            icon.setStyle("-fx-font-size: 14px;");

            Label typeLabel = new Label(item.eventType().name().replace('_', ' '));
            typeLabel.getStyleClass().addAll("label", "monospace");
            typeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            typeLabel.setTextFill(resolveColor(item.eventType()));
            typeLabel.setMinWidth(120);

            Label details = new Label(item.details());
            details.getStyleClass().addAll("label", "monospace", "text-secondary");
            details.setStyle("-fx-font-size: 11px;");
            HBox.setHgrow(details, Priority.ALWAYS);

            row.getChildren().addAll(timestamp, icon, typeLabel, details);
            setGraphic(row);
        }

        private String resolveIcon(AuditEvent.EventType type) {
            return switch (type) {
                case LOGIN_SUCCESS -> "✓";
                case LOGIN_FAILED -> "✗";
                case FILE_ENCRYPTED -> "\uD83D\uDD12";
                case FILE_DECRYPTED -> "\uD83D\uDD13";
                case FILE_DELETED -> "\uD83D\uDDD1";
                case FILE_OPENED -> "\uD83D\uDCC2";
                case VAULT_LOCKED -> "\uD83D\uDD10";
                case VAULT_UNLOCKED -> "\uD83D\uDD13";
                case PANIC_TRIGGERED -> "⚠";
                case PASSWORD_GENERATED -> "\uD83D\uDD11";
                case ACCOUNT_CREATED -> "⭐";
            };
        }

        private javafx.scene.paint.Color resolveColor(AuditEvent.EventType type) {
            return switch (type) {
                case LOGIN_SUCCESS, ACCOUNT_CREATED, VAULT_UNLOCKED ->
                        javafx.scene.paint.Color.web("#3fb950");
                case LOGIN_FAILED, PANIC_TRIGGERED ->
                        javafx.scene.paint.Color.web("#f85149");
                case FILE_ENCRYPTED, FILE_DECRYPTED, FILE_OPENED, PASSWORD_GENERATED ->
                        javafx.scene.paint.Color.web("#58a6ff");
                case FILE_DELETED, VAULT_LOCKED ->
                        javafx.scene.paint.Color.web("#d29922");
            };
        }
    }
}
