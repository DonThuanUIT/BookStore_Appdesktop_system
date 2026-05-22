package com.bookstore.frontend.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

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

    /** Hộp thoại xác nhận; trả về true nếu người dùng chọn OK. */
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Label label = new Label(message);
        label.setWrapText(true);
        label.setPrefWidth(320);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setStyle("-fx-font-size: 14px;");

        alert.getDialogPane().setContent(label);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Hộp thoại chọn số lượng khi thêm vào giỏ (1–999).
     * @return số lượng nếu bấm OK, rỗng nếu hủy
     */
    public static Optional<Integer> promptQuantityForCart(String bookTitle) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add to Cart");

        // Lấy link CSS - Hãy kiểm tra kỹ đường dẫn này có khớp với project của bạn không
        String colorsCss = AlertUtils.class.getResource("/com/bookstore/frontend/css/colors.css").toExternalForm();
        String themeCss = AlertUtils.class.getResource("/com/bookstore/frontend/css/theme.css").toExternalForm();

        // Nạp cả 2 file CSS vào DialogPane
        dialog.getDialogPane().getStylesheets().addAll(colorsCss, themeCss);
        dialog.getDialogPane().getStyleClass().add("custom-quantity-dialog");

        Label intro = new Label("Select Quantity:");
        intro.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 13px;");

        Label title = new Label(bookTitle);
        title.setWrapText(true);
        title.setPrefWidth(300);
        title.setAlignment(Pos.CENTER);
        // Sử dụng style có sẵn trong theme.css của bạn
        title.getStyleClass().add("book-card-title");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        spinner.setEditable(true);
        spinner.getStyleClass().add("modern-spinner");
        spinner.setPrefWidth(120);

        VBox box = new VBox(15, intro, title, spinner);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(25));
        // Ép nền tối trực tiếp cho VBox để chắc chắn không bị trắng
        box.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10;");

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        return dialog.showAndWait().flatMap(bt -> {
            if (bt != ButtonType.OK) return Optional.empty();
            commitSpinnerValue(spinner);
            return Optional.of(spinner.getValue());
        });
    }
    private static void commitSpinnerValue(Spinner<Integer> spinner) {
        if (!spinner.isEditable()) {
            return;
        }
        @SuppressWarnings("unchecked")
        SpinnerValueFactory.IntegerSpinnerValueFactory fac =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
        try {
            int val = Integer.parseInt(spinner.getEditor().getText().trim());
            val = Math.max(fac.getMin(), Math.min(fac.getMax(), val));
            fac.setValue(val);
        } catch (NumberFormatException e) {
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
        }
    }
}