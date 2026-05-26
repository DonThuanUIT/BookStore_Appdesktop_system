module com.bookstore.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires okhttp3;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    requires com.fasterxml.jackson.datatype.jsr310;

    requires static lombok;

    opens com.bookstore.frontend.controller to javafx.fxml;
    opens com.bookstore.frontend.components to javafx.fxml;

    opens com.bookstore.frontend.model to javafx.base, com.fasterxml.jackson.databind;
    opens com.bookstore.frontend.model.dto to javafx.base, com.fasterxml.jackson.databind;
    opens com.bookstore.frontend.model.dto.Request to javafx.base, com.fasterxml.jackson.databind;
    opens com.bookstore.frontend.model.dto.Response to com.fasterxml.jackson.databind, javafx.base;

    exports com.bookstore.frontend;
    exports com.bookstore.frontend.controller;
    exports com.bookstore.frontend.model;
    exports com.bookstore.frontend.navigation;
    exports com.bookstore.frontend.model.dto;
    exports com.bookstore.frontend.interactor;
    exports com.bookstore.frontend.model.dto.Response;
    exports com.bookstore.frontend.components;
}