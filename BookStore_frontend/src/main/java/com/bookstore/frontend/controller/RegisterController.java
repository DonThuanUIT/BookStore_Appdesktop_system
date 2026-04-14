package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.RegisterInteractor;
import com.bookstore.frontend.model.RegisterModel;
import com.bookstore.frontend.MainApplication;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField txtUsername, txtPasswordVisible, txtConfirmVisible;
    @FXML private PasswordField txtPassword, txtConfirmPassword;
    @FXML private Label lblMessage;
    @FXML private Button btnRegister;

    private RegisterModel model = new RegisterModel();
    private RegisterInteractor interactor = new RegisterInteractor(model);

    @FXML
    public void initialize() {
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtPassword.textProperty().bindBidirectional(model.passwordProperty());
        txtPasswordVisible.textProperty().bindBidirectional(model.passwordProperty());
        txtConfirmPassword.textProperty().bindBidirectional(model.confirmPasswordProperty());
        txtConfirmVisible.textProperty().bindBidirectional(model.confirmPasswordProperty());
        lblMessage.textProperty().bind(model.messageProperty());

        // Visibility logic
        txtPasswordVisible.visibleProperty().bind(model.passwordVisibleProperty());
        txtPassword.visibleProperty().bind(model.passwordVisibleProperty().not());
        txtConfirmVisible.visibleProperty().bind(model.confirmVisibleProperty());
        txtConfirmPassword.visibleProperty().bind(model.confirmVisibleProperty().not());

        btnRegister.disableProperty().bind(model.loadingProperty());
    }

    @FXML public void handleRegister() { interactor.register(); }
    @FXML public void togglePassword() { model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get()); }
    @FXML public void toggleConfirmPassword() { model.confirmVisibleProperty().set(!model.confirmVisibleProperty().get()); }
    @FXML
    public void navigateToLogin() {
        interactor.goToLogin();
    }}