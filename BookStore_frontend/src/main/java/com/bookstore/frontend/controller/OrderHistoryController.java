package com.bookstore.frontend.controller;

import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.model.dto.Response.OrderResponseDTO;
import com.bookstore.frontend.navigation.Navigatable;
import com.bookstore.frontend.util.UserSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.Optional;

public class OrderHistoryController implements Navigatable {
    @FXML private TableView<OrderResponseDTO> orderTable;
    @FXML private TableColumn<OrderResponseDTO, Long> colId;
    @FXML private TableColumn<OrderResponseDTO, String> colDate;
    @FXML private TableColumn<OrderResponseDTO, Double> colTotal;
    @FXML private TableColumn<OrderResponseDTO, String> colStatus;
    @FXML private TableColumn<OrderResponseDTO, String> colPayment;
    @FXML private TableColumn<OrderResponseDTO, Double> colDiscount;
    @FXML private TableColumn<OrderResponseDTO, Void> colAction;
    @FXML private Label lblTitle;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnAccept = new Button();
            {
                btnAccept.setStyle("-fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10; -fx-text-fill: white;");
                btnAccept.setOnAction(event -> {
                    OrderResponseDTO order = getTableView().getItems().get(getIndex());
                    handleStatusUpdate(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderResponseDTO order = getTableView().getItems().get(getIndex());
                    String status = order.getStatus();
                    boolean isAdmin = UserSession.getInstance().isAdminOrStaff();

                    // Hiển thị nút dựa trên trạng thái
                    if (isAdmin && ("PENDING".equalsIgnoreCase(status) || "SHIPPING".equalsIgnoreCase(status))) {
                        btnAccept.setText("PENDING".equalsIgnoreCase(status) ? "✓ Duyệt" : "✓ Hoàn tất");
                        btnAccept.setStyle("-fx-background-color: " + ("PENDING".equalsIgnoreCase(status) ? "#27ae60" : "#2980b9") + "; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10; -fx-text-fill: white;");
                        btnAccept.setVisible(true);
                        setGraphic(btnAccept);
                    } else {
                        btnAccept.setVisible(false);
                        setGraphic(null);
                    }
                }
            }
        });

        determineAndLoadData();
    }

    @Override
    public void onNavigate(Object data) {
        determineAndLoadData();
    }

    private void determineAndLoadData() {
        orderTable.getItems().clear();

        if (UserSession.getInstance().isAdminOrStaff()) {
            lblTitle.setText("Lịch sử Bán hàng (Quản trị)");
            colAction.setVisible(true);
            loadOrderHistory("/orders?page=0&size=20&sortBy=orderDate&direction=desc");
        } else {
            lblTitle.setText("Lịch sử Mua hàng của tôi");
            colAction.setVisible(false);
            loadOrderHistory("/orders/history?page=0&size=20");
        }
    }

    private void handleStatusUpdate(OrderResponseDTO order) {
        String nextStatus = "PENDING".equalsIgnoreCase(order.getStatus()) ? "SHIPPING" : "COMPLETED";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận trạng thái");
        alert.setHeaderText("Chuyển trạng thái đơn hàng #" + order.getId());
        alert.setContentText("Bạn có chắc chắn muốn chuyển đơn hàng sang trạng thái " + nextStatus + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateStatusAPI(order.getId(), nextStatus);
        }
    }

    private void updateStatusAPI(Long orderId, String status) {
        String jsonBody = "{\"status\": \"" + status + "\"}";
        ApiClient.getInstance().patch("/orders/" + orderId + "/status", jsonBody)
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(this::determineAndLoadData);
                    } else {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại!").show());
                    }
                });
    }

    private void loadOrderHistory(String endpoint) {
        ApiClient.getInstance().get(endpoint).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    ObjectMapper mapper = ApiClient.getInstance().getMapper();
                    JsonNode root = mapper.readTree(response.body());
                    List<OrderResponseDTO> orders = mapper.convertValue(root.get("content"), new TypeReference<List<OrderResponseDTO>>() {});
                    Platform.runLater(() -> orderTable.setItems(FXCollections.observableArrayList(orders)));
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }
}