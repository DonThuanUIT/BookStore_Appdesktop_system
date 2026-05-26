module com.bookstore.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires javafx.base;

    exports com.bookstore.frontend;
    exports com.bookstore.frontend.controller;
    exports com.bookstore.frontend.model;
    exports com.bookstore.frontend.interactor;

    opens com.bookstore.frontend.controller to javafx.fxml;
    opens com.bookstore.frontend.model to javafx.base;
    opens com.bookstore.frontend.interactor to javafx.fxml;
}