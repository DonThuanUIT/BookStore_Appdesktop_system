module com.bookstore.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.bookstore.frontend to javafx.fxml;

    exports com.bookstore.frontend;
}