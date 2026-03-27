package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.LoginInteractor;
import com.bookstore.frontend.model.LoginModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private LoginModel model;
    private LoginInteractor interactor;

    public LoginController() {
        this.model = new LoginModel();
        this.interactor = new LoginInteractor(this.model);
    }

    @FXML
    public void initialize() {

        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtPassword.textProperty().bindBidirectional(model.passwordProperty());

        lblMessage.textProperty().bind(model.messageProperty());
    }

    @FXML
    public void handleLogin() {
        interactor.login();
    }
}