package com.bookstore.frontend.util;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BadgeIcon extends StackPane {
    private final Label lblCount = new Label("0");

    public BadgeIcon(String iconName) {
        // Giả sử bạn dùng FontIcon hoặc ImageView cho icon User
        Label icon = new Label(iconName);
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        // Badge tròn
        Circle badge = new Circle(8, Color.RED);
        lblCount.setStyle("-fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold;");

        StackPane badgePane = new StackPane(badge, lblCount);
        badgePane.setMaxSize(16, 16);
        // Đặt vị trí badge ở góc trên bên phải
        StackPane.setAlignment(badgePane, javafx.geometry.Pos.TOP_RIGHT);
        badgePane.setTranslateX(5);
        badgePane.setTranslateY(-5);
        badgePane.setVisible(false); // Ẩn mặc định

        this.getChildren().addAll(icon, badgePane);
    }

    public void updateCount(int count) {
        lblCount.setText(String.valueOf(count));
        this.getChildren().get(1).setVisible(count > 0); // Chỉ hiện nếu > 0
    }
}