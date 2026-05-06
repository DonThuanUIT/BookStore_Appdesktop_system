package com.bookstore.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ForgotPasswordController {

    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;
    @FXML private VBox forgotPasswordForm, loadingContainer;
    @FXML private TextField txtEmail;
    @FXML private Button btnSendResetLink;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        // Ràng buộc ảnh nền luôn full màn hình
        backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());
    }

    @FXML
    public void handleSendResetLink() {
        // TODO: Chuyển logic này cho ForgotPasswordInteractor xử lý sau
        System.out.println("Sẽ gửi link khôi phục đến email: " + txtEmail.getText());
    }

    @FXML
    public void goToLogin() {
        // TODO: Gọi SceneManager (hoặc Interactor) để chuyển cảnh về lại LoginView
        System.out.println("Điều hướng trở lại màn hình Login...");
    }
}