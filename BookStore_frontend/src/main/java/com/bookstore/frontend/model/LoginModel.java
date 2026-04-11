package com.bookstore.frontend.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LoginModel {
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    // Quản lý trạng thái hiển thị mật khẩu
    private final BooleanProperty passwordVisible = new SimpleBooleanProperty(false);

    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty messageProperty() { return message; }
    public BooleanProperty passwordVisibleProperty() { return passwordVisible; }
}