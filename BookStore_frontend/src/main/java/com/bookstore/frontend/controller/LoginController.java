package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.LoginInteractor;
import com.bookstore.frontend.model.LoginModel;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;
    @FXML private VBox loginForm, loadingContainer;
    @FXML private TextField txtUsername, txtPasswordVisible;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin, btnTogglePassword;
    @FXML private Label lblMessage;

    private LoginModel model;
    private LoginInteractor interactor;

    public LoginController() {
        this.model = new LoginModel();
        this.interactor = new LoginInteractor(this.model);
    }

    @FXML
    public void initialize() {
        backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

        txtUsername.textProperty().bindBidirectional(model.usernameProperty());
        lblMessage.textProperty().bind(model.messageProperty());

        // Xử lý mật khẩu thông minh
        setupSmartVisibility(txtPassword, txtPasswordVisible, btnTogglePassword, model.passwordVisibleProperty());

        model.loadingProperty().addListener((obs, old, isLoading) -> {
            btnLogin.setVisible(!isLoading);
            btnLogin.setManaged(!isLoading);
            loadingContainer.setVisible(isLoading);
            loadingContainer.setManaged(isLoading);
            txtUsername.setDisable(isLoading);
            txtPassword.setDisable(isLoading);
        });
    }

    private void setupSmartVisibility(PasswordField pf, TextField tf, Button btn, BooleanProperty visibleProp) {
        tf.textProperty().bindBidirectional(pf.textProperty());
        Runnable updateUI = () -> {
            boolean showText = visibleProp.get() && (pf.getText() != null && !pf.getText().isEmpty());
            tf.setVisible(showText);
            tf.setManaged(showText);
            pf.setVisible(!showText);
            pf.setManaged(!showText);
            btn.setText(visibleProp.get() ? "🙈" : "👁");
        };
        visibleProp.addListener((obs, old, newVal) -> updateUI.run());
        pf.textProperty().addListener((obs, old, newVal) -> updateUI.run());
    }

    @FXML public void handleLogin() { interactor.login(); }
    @FXML public void togglePassword() { interactor.togglePasswordVisibility(); }
    @FXML public void handleNavigateToRegister() { interactor.navigateToRegister(); }
}