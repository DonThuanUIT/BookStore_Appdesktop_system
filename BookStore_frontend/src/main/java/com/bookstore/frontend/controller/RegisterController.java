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
    @FXML private TextField txtUsername, txtEmail, txtAddress, txtPasswordVisible, txtConfirmPasswordVisible;
    @FXML private PasswordField txtPassword, txtConfirmPassword;
    @FXML private Button btnTogglePassword, btnToggleConfirm, btnRegister;
    @FXML private ComboBox<String> cbRole;
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
        cbRole.setValue("CUSTOMER");

        // Bindings thông thường
        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        txtEmail.textProperty().bindBidirectional(model.emailProperty());
        txtAddress.textProperty().bindBidirectional(model.addressProperty());
        cbRole.valueProperty().bindBidirectional(model.roleProperty());
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
        tf.textProperty().bindBidirectional(pf.textProperty());
        Runnable updateUI = () -> {
            boolean isVisible = visibleProp.get();
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