package com.bookstore.frontend.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HomeModel {
    private final StringProperty currentUser = new SimpleStringProperty("");
    private final StringProperty welcomeMessage = new SimpleStringProperty("");

    public StringProperty currentUserProperty() {
        return currentUser;
    }

    public StringProperty welcomeMessageProperty() {
        return welcomeMessage;
    }

    // Business Logic for model
    public void setWelcome(String username) {
        this.currentUser.set(username);
        this.welcomeMessage.set("Welcome back, " + username + "!");
    }
}