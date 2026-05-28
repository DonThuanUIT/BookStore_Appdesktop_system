package com.bookstore.frontend.model;

import javafx.beans.property.*;

public class RegisterModel {
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty confirmPassword = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty otp = new SimpleStringProperty("");
    private final StringProperty countdownText = new SimpleStringProperty("03:00");
    private final BooleanProperty otpRequested = new SimpleBooleanProperty(false);

    private final BooleanProperty passwordVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty confirmVisible = new SimpleBooleanProperty(false);

    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty confirmPasswordProperty() { return confirmPassword; }
    public StringProperty messageProperty() { return message; }
    public BooleanProperty loadingProperty() { return loading; }
    public BooleanProperty passwordVisibleProperty() { return passwordVisible; }
    public BooleanProperty confirmVisibleProperty() { return confirmVisible; }
    public StringProperty emailProperty() { return email; }
    public StringProperty otpProperty() { return otp; }
    public StringProperty countdownTextProperty() { return countdownText; }
    public BooleanProperty otpRequestedProperty() { return otpRequested; }
}