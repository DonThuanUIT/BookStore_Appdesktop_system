package com.bookstore.frontend.model;

import javafx.beans.property.*;

public class RegisterModel {
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty confirmPassword = new SimpleStringProperty("");
    private final StringProperty role = new SimpleStringProperty("CUSTOMER"); // Mặc định là CUSTOMER

    private final StringProperty message = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty passwordVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty confirmVisible = new SimpleBooleanProperty(false);

    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty confirmPasswordProperty() { return confirmPassword; }
    public StringProperty roleProperty() { return role; }
    public StringProperty messageProperty() { return message; }
    public BooleanProperty loadingProperty() { return loading; }
    public BooleanProperty passwordVisibleProperty() { return passwordVisible; }
    public BooleanProperty confirmVisibleProperty() { return confirmVisible; }
}