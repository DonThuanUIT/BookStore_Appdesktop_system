package com.bookstore.frontend.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class AlertUtils {
    public static void show(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);

        alert.setHeaderText(null);

        Label label = new Label(content);
        label.setWrapText(true);
        label.setPrefWidth(300);

        label.setAlignment(Pos.CENTER_LEFT);
        label.setStyle("-fx-text-alignment: left; -fx-padding: 0 0 0 0; -fx-font-size: 14px;");

        alert.getDialogPane().setContent(label);

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}