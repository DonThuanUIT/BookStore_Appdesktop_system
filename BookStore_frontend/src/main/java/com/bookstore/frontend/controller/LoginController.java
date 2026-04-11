package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.LoginInteractor;
import com.bookstore.frontend.model.LoginModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LoginController {
    // Các ID phải khớp chính xác với fx:id trong file FXML
    @FXML private HBox rootNode;
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
        // Binding dữ liệu giữa UI và Model
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtPassword.textProperty().bindBidirectional(model.passwordProperty());
        lblMessage.textProperty().bind(model.messageProperty());
    }

    @FXML
    public void handleLogin() {
        interactor.login();
    }

    @FXML
    private void handleMaximize(ActionEvent event) {
        if (rootNode != null && rootNode.getScene() != null) {
            Stage stage = (Stage) rootNode.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        }
    }
}