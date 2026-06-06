package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.dto.Response.OrderDetailResponseDTO;
import com.bookstore.frontend.model.dto.Response.OrderResponseDTO;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class OrderDetailSidePanelController {

    @FXML private VBox sidePanelRoot;
    @FXML private Label lblOrderId;
    @FXML private Label lblDate;
    @FXML private Label lblPaymentMethod;
    @FXML private Label lblStatus;

    @FXML private Label lblCustomerUser;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerEmail;
    @FXML private Label lblCustomerPhone;
    @FXML private Label lblCustomerAddress;

    @FXML private TableView<OrderDetailResponseDTO> tvDetails;
    @FXML private TableColumn<OrderDetailResponseDTO, String> colTitle;
    @FXML private TableColumn<OrderDetailResponseDTO, Integer> colQty;
    @FXML private TableColumn<OrderDetailResponseDTO, Double> colPrice;
    @FXML private TableColumn<OrderDetailResponseDTO, Double> colTotal;

    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalCost;

    private boolean isShowing = false;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                // FIX: Sử dụng CurrencyUtils để hiển thị VND thống nhất
                else setText(com.bookstore.frontend.util.CurrencyUtils.formatVND(price));
            }
        });

        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else {
                    // FIX: Sử dụng CurrencyUtils để hiển thị VND thống nhất
                    setText(com.bookstore.frontend.util.CurrencyUtils.formatVND(total));
                    setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-alignment: center-right;");
                }
            }
        });
    }

    public void setOrderDataAndShow(OrderResponseDTO order) {
        lblOrderId.setText("#ORD-" + order.getId());
        
        java.time.LocalDateTime date = order.getOrderDate();
        if (date != null) {
            lblDate.setText(date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            lblDate.setText("N/A");
        }

        lblPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "Tiền mặt");
        lblStatus.setText(order.getStatus() != null ? translateStatus(order.getStatus()) : "N/A");
        
        // Cập nhật màu sắc dựa trên trạng thái
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if ("PENDING".equals(status)) {
            lblStatus.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
        } else if ("SHIPPING".equals(status)) {
            lblStatus.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
        } else if ("COMPLETED".equals(status)) {
            lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            lblStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }

        // Cập nhật thông tin khách hàng
        if (order.getUser() != null) {
            lblCustomerUser.setText(order.getUser().username() != null ? order.getUser().username() : "N/A");
            lblCustomerName.setText(order.getUser().fullName() != null ? order.getUser().fullName() : "N/A");
            lblCustomerEmail.setText(order.getUser().email() != null ? order.getUser().email() : "N/A");
            lblCustomerPhone.setText(order.getUser().phone() != null ? order.getUser().phone() : "N/A");
            lblCustomerAddress.setText(order.getUser().address() != null ? order.getUser().address() : "N/A");
        } else {
            lblCustomerUser.setText("N/A");
            lblCustomerName.setText("N/A");
            lblCustomerEmail.setText("N/A");
            lblCustomerPhone.setText("N/A");
            lblCustomerAddress.setText("N/A");
        }

        // Load items
        if (order.getDetails() != null) {
            tvDetails.setItems(FXCollections.observableArrayList(order.getDetails()));
        } else {
            tvDetails.setItems(FXCollections.emptyObservableList());
        }

        // Tính toán các giá trị tổng
        double finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : 0.0;
        double discount = order.getDiscount() != null ? order.getDiscount() : 0.0;
        double subtotal = finalAmount + discount;

        // FIX: Sử dụng CurrencyUtils để hiển thị VND thống nhất
        lblSubtotal.setText(com.bookstore.frontend.util.CurrencyUtils.formatVND(subtotal));
        lblDiscount.setText("- " + com.bookstore.frontend.util.CurrencyUtils.formatVND(discount));
        lblTotalCost.setText(com.bookstore.frontend.util.CurrencyUtils.formatVND(finalAmount));

        if (!isShowing) {
            slidePanel(0);
            isShowing = true;
        }
    }

    @FXML
    public void handleClose() {
        if (isShowing) {
            slidePanel(sidePanelRoot.getPrefWidth());
            isShowing = false;
        }
    }

    private void slidePanel(double targetX) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidePanelRoot);
        transition.setToX(targetX);
        transition.play();
    }

    private String translateStatus(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "Chờ duyệt";
            case "SHIPPING": return "Đang giao";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELED": return "Đã hủy";
            default: return status;
        }
    }
}
