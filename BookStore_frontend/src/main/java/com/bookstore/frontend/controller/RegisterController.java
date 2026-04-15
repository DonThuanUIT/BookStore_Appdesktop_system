package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.RegisterInteractor;
import com.bookstore.frontend.model.RegisterModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;

    private RegisterModel model;
    private RegisterInteractor interactor;

    public RegisterController() {
        this.model = new RegisterModel();
        this.interactor = new RegisterInteractor(this.model);
    }

    @FXML
    public void initialize() {
        txtFullName.textProperty().bindBidirectional(model.fullNameProperty());
        txtEmail.textProperty().bindBidirectional(model.emailProperty());
        txtPassword.textProperty().bindBidirectional(model.passwordProperty());
        txtConfirmPassword.textProperty().bindBidirectional(model.confirmPasswordProperty());

        lblMessage.textProperty().bind(model.messageProperty());
    }

    @FXML
    public void handleRegister() {
        interactor.register();
    }

    @FXML
    public void goToLogin() {
        model.setMessage("Navigate back to login screen.");
    }
}