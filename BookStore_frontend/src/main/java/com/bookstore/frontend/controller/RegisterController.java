package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.RegisterInteractor;
import com.bookstore.frontend.model.RegisterModel;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RegisterController {
    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;
    @FXML private VBox registerForm, loadingProgress;

    // Đã xóa txtEmail, txtAddress
    @FXML private TextField txtUsername, txtPasswordVisible, txtConfirmPasswordVisible;
    @FXML private PasswordField txtPassword, txtConfirmPassword;

    // Đã xóa cbRole
    @FXML private Button btnTogglePassword, btnToggleConfirm, btnRegister;
    @FXML private Label lblMessage;

    private RegisterModel model;
    private RegisterInteractor interactor;

    public RegisterController() {
        this.model = new RegisterModel();
        this.interactor = new RegisterInteractor(this.model);
    }

    @FXML
    public void initialize() {
        // Ràng buộc kích thước ảnh nền
        backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

        // Bindings thông thường (Đã xóa email, address, role)
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtPassword.textProperty().bindBidirectional(model.passwordProperty());
        txtConfirmPassword.textProperty().bindBidirectional(model.confirmPasswordProperty());
        lblMessage.textProperty().bind(model.messageProperty());

        // Xử lý mật khẩu thông minh (giữ placeholder) cho từng ô
        setupSmartVisibility(txtPassword, txtPasswordVisible, btnTogglePassword, model.passwordVisibleProperty());
        setupSmartVisibility(txtConfirmPassword, txtConfirmPasswordVisible, btnToggleConfirm, model.confirmVisibleProperty());

        // Hiệu ứng Loading hoán đổi nút bấm
        model.loadingProperty().addListener((obs, old, isLoading) -> {
            btnRegister.setVisible(!isLoading);
            btnRegister.setManaged(!isLoading);
            loadingProgress.setVisible(isLoading);
            loadingProgress.setManaged(isLoading);
            registerForm.setDisable(isLoading);
        });
    }

    private void setupSmartVisibility(PasswordField pf, TextField tf, Button btn, BooleanProperty visibleProp) {
        // Đồng bộ dữ liệu giữa PasswordField và TextField
        // LƯU Ý: Rút kinh nghiệm từ JavaFX, để bindBidirectional chạy mượt giữa 2 ô text,
        // ta set giá trị khởi tạo trước khi bind để tránh vòng lặp (loop).
        tf.setText(pf.getText());
        tf.textProperty().bindBidirectional(pf.textProperty());

        Runnable updateUI = () -> {
            boolean isVisible = visibleProp.get();
            // Chỉ hiện ô TextField (chữ thường) khi được bật mắt và CÓ TEXT
            boolean hasText = pf.getText() != null && !pf.getText().isEmpty();
            boolean showText = isVisible && hasText;

            tf.setVisible(showText);
            tf.setManaged(showText);
            pf.setVisible(!showText);
            pf.setManaged(!showText);
            btn.setText(isVisible ? "🙈" : "👁");
        };

        visibleProp.addListener((obs, old, newVal) -> updateUI.run());
        pf.textProperty().addListener((obs, old, newVal) -> updateUI.run());
    }

    @FXML public void togglePassword() { model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get()); }
    @FXML public void toggleConfirm() { model.confirmVisibleProperty().set(!model.confirmVisibleProperty().get()); }
    @FXML public void handleRegister() { interactor.register(); }
    @FXML public void goToLogin() { interactor.navigateToLogin(); }
}