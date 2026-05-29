package com.bookstore.frontend.components;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.util.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;

public class AccountPopup extends Popup {

    private final Label lblName = new Label();
    private final Label lblEmail = new Label();

    public AccountPopup() {
        this.setAutoHide(true);

        VBox container = new VBox(5);
        container.setPrefWidth(260);
        container.setStyle("-fx-background-color: linear-gradient(to bottom, #1e2430, #151a22);"
                + "-fx-background-radius: 16; -fx-border-color: rgba(255,199,7,0.35);"
                + "-fx-border-width: 1; -fx-padding: 15;");

        lblName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        lblEmail.setStyle("-fx-text-fill: #9aa5b5; -fx-font-size: 11px;");

        Button btnProfile = createMenuButton("Thông tin tài khoản");
        btnProfile.setOnAction(e -> {
            NavigationService.getInstance().navigateTo(PageType.PROFILE);
            this.hide();
        });

        Button btnHistory = createMenuButton("Lịch sử đơn hàng");
        btnHistory.setOnAction(e -> {
            NavigationService.getInstance().navigateTo(PageType.ORDER_HISTORY);
            this.hide();
        });

        Button btnLogout = new Button("Đăng xuất");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-text-fill: #ff5555; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10 0 0 0;");

        btnLogout.setOnAction(e -> {
            // 1. Xóa session
            UserSession.getInstance().clean();

            NavigationService.getInstance().clearCache();

            try {
                MainApplication.showView("LoginView.fxml", "BookStore - Login");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            this.hide();
        });

        container.getChildren().addAll(lblName, lblEmail, new Separator(), btnProfile, btnHistory, new Separator(), btnLogout);
        this.getContent().add(container);
    }

    public void refresh() {
        UserSession session = UserSession.getInstance();
        if (session.getUsername() != null) {
            lblName.setText(session.getUsername());
            lblEmail.setText("User Account");
        } else {
            lblName.setText("Guest");
            lblEmail.setText("Not Logged In");
        }
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 8;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #FFCC00;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;"));
        return btn;
    }
}