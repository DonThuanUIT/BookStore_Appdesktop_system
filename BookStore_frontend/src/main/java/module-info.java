module com.bookstore.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.bookstore.frontend.controller to javafx.fxml;

    opens com.bookstore.frontend.model to javafx.base;

    exports com.bookstore.frontend;
    exports com.bookstore.frontend.controller;
    exports com.bookstore.frontend.model;

    exports com.bookstore.frontend.navigation;
}