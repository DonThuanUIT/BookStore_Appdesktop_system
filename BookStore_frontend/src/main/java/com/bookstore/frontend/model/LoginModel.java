package com.bookstore.frontend.model;

import javafx.beans.property.*;

public class LoginModel {
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    private final BooleanProperty passwordVisible = new SimpleBooleanProperty(false);
    // Quản lý trạng thái đợi phản hồi từ HTTP API
    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty messageProperty() { return message; }
    public BooleanProperty passwordVisibleProperty() { return passwordVisible; }
    public BooleanProperty loadingProperty() { return loading; }
}