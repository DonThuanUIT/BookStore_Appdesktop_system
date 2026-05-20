package com.bookstore.frontend.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class PaymentController {

    @FXML private HBox methodCod, methodBank;
    @FXML private VBox detailsCod, detailsBank;
    @FXML private ImageView checkCod, checkBank, qrImageView;

    private Supplier<CompletableFuture<Boolean>> onConfirmCallback;
    private String selectedMethod = "COD";

    @FXML
    public void initialize() {
        methodCod.setOnMouseClicked(e -> selectMethod("COD"));
        methodBank.setOnMouseClicked(e -> selectMethod("BANK"));

        // Mặc định chọn COD
        selectMethod("COD");
    }

    private void selectMethod(String method) {
        selectedMethod = method;

        boolean isCod = method.equals("COD");
        boolean isBank = method.equals("BANK");

        detailsCod.setVisible(isCod);
        detailsCod.setManaged(isCod);

        detailsBank.setVisible(isBank);
        detailsBank.setManaged(isBank);

        checkCod.setVisible(isCod);
        checkBank.setVisible(isBank);

        methodCod.getStyleClass().remove("payment-method-row-selected");
        methodBank.getStyleClass().remove("payment-method-row-selected");

        if (isCod) {
            methodCod.getStyleClass().add("payment-method-row-selected");
        } else {
            methodBank.getStyleClass().add("payment-method-row-selected");
        }
    }

    @FXML
    private void handleZoomQR() {
        try {
            if (qrImageView.getImage() == null) return;

            Stage zoomStage = new Stage();
            zoomStage.initModality(Modality.APPLICATION_MODAL);
            zoomStage.initStyle(StageStyle.TRANSPARENT);

            ImageView largeQr = new ImageView(qrImageView.getImage());
            largeQr.setFitWidth(420);
            largeQr.setFitHeight(560);
            largeQr.setPreserveRatio(true);
            largeQr.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0, 0, 0);");

            // Click vào ảnh phóng to một lần nữa để tắt popup nhanh
            largeQr.setOnMouseClicked(e -> zoomStage.close());

            StackPane rootPane = new StackPane(largeQr);
            rootPane.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(rootPane, Color.TRANSPARENT);
            zoomStage.setScene(scene);

            Stage currentStage = (Stage) qrImageView.getScene().getWindow();
            zoomStage.setX(currentStage.getX() + (currentStage.getWidth() - 420) / 2);
            zoomStage.setY(currentStage.getY() + (currentStage.getHeight() - 560) / 2);

            zoomStage.show();
        } catch (Exception e) {
            System.err.println("Lỗi hiển thị phóng to mã QR: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfirm() {
        System.out.println("Payment Method Selected: " + selectedMethod);

        if (onConfirmCallback != null) {
            // Khóa tương tác tránh spam click
            methodCod.setDisable(true);
            methodBank.setDisable(true);

            onConfirmCallback.get().thenAccept(isSuccess -> {
                Platform.runLater(() -> {
                    if (isSuccess) {
                        System.out.println("Order saved successfully via API. Closing payment window.");
                        handleClose();
                    } else {
                        System.out.println("Failed to save order. Keeping payment window open.");
                        methodCod.setDisable(false);
                        methodBank.setDisable(false);

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi hệ thống");
                        alert.setHeaderText(null);
                        alert.setContentText("Không thể tạo đơn hàng. Vui lòng kiểm tra lại kết nối hoặc số lượng sách trong kho!");
                        alert.showAndWait();
                    }
                });
            });
        } else {
            handleClose();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) methodCod.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    public void setOnConfirm(Supplier<CompletableFuture<Boolean>> callback) {
        this.onConfirmCallback = callback;
    }

    public String getSelectedMethod() {
        return selectedMethod;
    }
}