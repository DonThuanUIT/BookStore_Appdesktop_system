package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.LoginInteractor;
import com.bookstore.frontend.model.LoginModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class LoginController {
    @FXML private HBox rootNode;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnTogglePassword;
    @FXML private Label lblMessage;

    private LoginModel model;
    private LoginInteractor interactor;

    public LoginController() {
        this.model = new LoginModel();
        this.interactor = new LoginInteractor(this.model);
    }

    @FXML
    public void initialize() {
        // 1. Binding Username và Message
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        lblMessage.textProperty().bind(model.messageProperty());

        txtPassword.textProperty().bindBidirectional(model.passwordProperty());
        txtPasswordVisible.textProperty().bindBidirectional(model.passwordProperty());


        txtPasswordVisible.visibleProperty().bind(model.passwordVisibleProperty());
        txtPassword.visibleProperty().bind(model.passwordVisibleProperty().not());

        model.passwordVisibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                btnTogglePassword.setText("🙈");
            } else {
                btnTogglePassword.setText("👁");
            }
        });
    }

    @FXML
    public void handleLogin() {
        interactor.login();
    }

    @FXML
    public void togglePassword() {
        interactor.togglePasswordVisibility();
    }
}