package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.LoginInteractor;
import com.bookstore.frontend.model.LoginModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnLogin; // Đã thêm fx:id trong FXML để hết lỗi Null
    @FXML private Label lblMessage;

    private final LoginModel model = new LoginModel();
    private final LoginInteractor interactor = new LoginInteractor(model);

    @FXML
    public void initialize() {
        // 1. Bind dữ liệu giữa View và Model
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtPassword.textProperty().bindBidirectional(model.passwordProperty());
        txtPasswordVisible.textProperty().bindBidirectional(model.passwordProperty());
        lblMessage.textProperty().bind(model.messageProperty());

        // 2. Logic hiển thị (UI Logic)
        txtPasswordVisible.visibleProperty().bind(model.passwordVisibleProperty());
        txtPassword.visibleProperty().bind(model.passwordVisibleProperty().not());

        // 3. Vô hiệu hóa nút khi đang xử lý (Event Logic)
        btnLogin.disableProperty().bind(model.loadingProperty());
    }

    @FXML
    public void handleLogin() {
        interactor.login(); // Chuyển business logic cho Interactor
    }

    @FXML
    public void togglePassword() {
        interactor.togglePassword();
    }

    @FXML
    public void navigateToRegister() {
        interactor.navigateToRegister();
    }
}