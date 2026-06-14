module com.chavemestra {
    requires javafx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;

    opens com.chavemestra.model to com.fasterxml.jackson.databind;

    exports com.chavemestra.app;
    exports com.chavemestra.model;
    exports com.chavemestra.crypto;
    exports com.chavemestra.security;
    exports com.chavemestra.storage;
    exports com.chavemestra.controller;
    exports com.chavemestra.ui;
    exports com.chavemestra.utils;
}
