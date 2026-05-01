module com.bookstore.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires okhttp3;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    opens com.bookstore.frontend.controller to javafx.fxml;
    opens com.bookstore.frontend.model to javafx.base;
    opens com.bookstore.frontend.model.dto to javafx.base, com.fasterxml.jackson.databind;

    exports com.bookstore.frontend;
    exports com.bookstore.frontend.controller;
    exports com.bookstore.frontend.model;
    exports com.bookstore.frontend.navigation;
    exports com.bookstore.frontend.model.dto;
    exports com.bookstore.frontend.interactor;
}