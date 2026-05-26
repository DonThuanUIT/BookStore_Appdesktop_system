package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.RegisterInteractor;
import com.bookstore.frontend.model.RegisterModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private TextField passwordVisible;
    @FXML private PasswordField confirmPassword;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Label lblMessage;
    @FXML private Button btnRegister;
    @FXML private ComboBox<String> role; // Vẫn giữ để khớp FXML nhưng sẽ để trống

    private final RegisterModel model = new RegisterModel();
    private final RegisterInteractor interactor = new RegisterInteractor(model);

    @FXML
    public void initialize() {
        // 1. Binding dữ liệu người dùng nhập
        username.textProperty().bindBidirectional(model.usernameProperty());
        password.textProperty().bindBidirectional(model.passwordProperty());
        confirmPassword.textProperty().bindBidirectional(model.confirmPasswordProperty());

        // 2. Binding cho các trường hiển thị text (khi nhấn nút Show)
        passwordVisible.textProperty().bindBidirectional(model.passwordProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(model.confirmPasswordProperty());

        // 3. Binding trạng thái loading và thông báo
        lblMessage.textProperty().bind(model.messageProperty());
        btnRegister.disableProperty().bind(model.loadingProperty());

        // 4. Logic UI: Tự động ẩn/hiện dựa trên trạng thái trong Model
        passwordVisible.visibleProperty().bind(model.passwordVisibleProperty());
        password.visibleProperty().bind(model.passwordVisibleProperty().not());

        confirmPasswordVisible.visibleProperty().bind(model.confirmPasswordVisibleProperty());
        confirmPassword.visibleProperty().bind(model.confirmPasswordVisibleProperty().not());

        // Gợi ý: Set text mặc định cho ComboBox role nếu muốn người dùng thấy
        if(role != null) role.setPromptText("CUSTOMER (Default)");
    }

    @FXML public void handleRegister() { interactor.register(); }
    @FXML public void navigateToLogin() { interactor.goToLogin(); }
    @FXML public void togglePassword() { interactor.togglePassword(); }
    @FXML public void toggleConfirmPassword() { interactor.toggleConfirmPassword(); }
}