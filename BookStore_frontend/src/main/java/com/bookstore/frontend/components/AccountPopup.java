package com.bookstore.frontend.components;

import com.bookstore.frontend.api.ApiClient;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.util.UserSession;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class AccountPopup extends Popup {

    // Không cần UserService hay OrderService vì đã có ApiClient
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

        Button btnProfile = createMenuButton("Profile Settings");
        btnProfile.setOnAction(e -> {
            NavigationService.getInstance().navigateTo(PageType.PROFILE);
            this.hide();
        });

        Button btnHistory = createMenuButton("Purchase History");
        btnHistory.setOnAction(e -> {
            NavigationService.getInstance().navigateTo(PageType.ORDER_HISTORY);
            this.hide();
        });

        Button btnLogout = new Button("Log Out");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-text-fill: #ff5555; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 10 0 0 0;");
        btnLogout.setOnAction(e -> {
            UserSession.getInstance().clean();
            NavigationService.getInstance().navigateTo(PageType.LOGIN);
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
            // Sau này nếu cần lấy thông tin chi tiết hơn, bạn dùng ApiClient.getInstance().get("/users/profile") tại đây
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