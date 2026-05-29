package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.RegisterInteractor;
import com.bookstore.frontend.model.RegisterModel;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RegisterController {
    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;
    @FXML private VBox registerForm, loadingProgress;

    @FXML private TextField txtUsername, txtEmail, txtOtp;
    @FXML private Button btnRequestOtp;
    @FXML private Label lblCountdown;

    // Các container UI mới khai báo
    @FXML private HBox otpContainer;
    @FXML private StackPane passwordContainer, confirmPasswordContainer, registerBtnContainer;

    @FXML private TextField txtPasswordVisible, txtConfirmPasswordVisible;
    @FXML private PasswordField txtPassword, txtConfirmPassword;

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
        backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtEmail.textProperty().bindBidirectional(model.emailProperty());
        txtOtp.textProperty().bindBidirectional(model.otpProperty());
        txtPassword.textProperty().bindBidirectional(model.passwordProperty());
        txtConfirmPassword.textProperty().bindBidirectional(model.confirmPasswordProperty());
        lblMessage.textProperty().bind(model.messageProperty());
        lblCountdown.textProperty().bind(model.countdownTextProperty());

        // --- BẮT ĐẦU: LOGIC ẨN HIỆN THÔNG MINH ---

        // 1. Khóa Username và Email sau khi đã nhấn gửi OTP (Không cho sửa nữa)
        txtUsername.disableProperty().bind(model.otpRequestedProperty());
        txtEmail.disableProperty().bind(model.otpRequestedProperty());

        // 2. Đảo đổi giữa nút Nhận OTP và Đồng hồ đếm ngược
        btnRequestOtp.visibleProperty().bind(model.otpRequestedProperty().not());
        btnRequestOtp.managedProperty().bind(model.otpRequestedProperty().not());
        lblCountdown.visibleProperty().bind(model.otpRequestedProperty());
        lblCountdown.managedProperty().bind(model.otpRequestedProperty());

        // 3. Chỉ hiện OTP, Mật Khẩu, Nút Đăng ký SAU KHI đã gửi OTP
        otpContainer.visibleProperty().bind(model.otpRequestedProperty());
        otpContainer.managedProperty().bind(model.otpRequestedProperty());
        passwordContainer.visibleProperty().bind(model.otpRequestedProperty());
        passwordContainer.managedProperty().bind(model.otpRequestedProperty());
        confirmPasswordContainer.visibleProperty().bind(model.otpRequestedProperty());
        confirmPasswordContainer.managedProperty().bind(model.otpRequestedProperty());
        registerBtnContainer.visibleProperty().bind(model.otpRequestedProperty());
        registerBtnContainer.managedProperty().bind(model.otpRequestedProperty());

        // --- KẾT THÚC LOGIC ---

        setupSmartVisibility(txtPassword, txtPasswordVisible, btnTogglePassword, model.passwordVisibleProperty());
        setupSmartVisibility(txtConfirmPassword, txtConfirmPasswordVisible, btnToggleConfirm, model.confirmVisibleProperty());

        model.loadingProperty().addListener((obs, old, isLoading) -> {
            btnRegister.setVisible(!isLoading);
            btnRegister.setManaged(!isLoading);
            loadingProgress.setVisible(isLoading);
            loadingProgress.setManaged(isLoading);
            registerForm.setDisable(isLoading);
        });
    }

    private void setupSmartVisibility(PasswordField pf, TextField tf, Button btn, BooleanProperty visibleProp) {
        tf.setText(pf.getText());
        tf.textProperty().bindBidirectional(pf.textProperty());

        Runnable updateUI = () -> {
            boolean isVisible = visibleProp.get();
            boolean hasText = pf.getText() != null && !pf.getText().isEmpty();
            boolean showText = isVisible && hasText;

            tf.setVisible(showText);
            tf.setManaged(showText);
            pf.setVisible(!showText);
            pf.setManaged(!showText);
            btn.setText(isVisible ? "📖" : "📕");
        };

        visibleProp.addListener((obs, old, newVal) -> updateUI.run());
        pf.textProperty().addListener((obs, old, newVal) -> updateUI.run());
        updateUI.run();
    }

    @FXML public void togglePassword() { model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get()); }
    @FXML public void toggleConfirm() { model.confirmVisibleProperty().set(!model.confirmVisibleProperty().get()); }
    @FXML public void handleRegister() { interactor.register(); }
    @FXML public void goToLogin() { interactor.navigateToLogin(); }
    @FXML public void handleRequestOtp() { interactor.requestOtp(); }
}