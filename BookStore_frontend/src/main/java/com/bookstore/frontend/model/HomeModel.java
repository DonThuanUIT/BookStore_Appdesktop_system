package com.bookstore.frontend.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HomeModel {
    private final StringProperty welcomeMessage = new SimpleStringProperty("");

    public StringProperty welcomeMessageProperty() {
        return welcomeMessage;
    }
}
