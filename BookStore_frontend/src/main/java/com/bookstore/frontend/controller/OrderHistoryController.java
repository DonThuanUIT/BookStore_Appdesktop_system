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

        setupActionColumn();
        determineAndLoadData();
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnAction = new Button();
            {
                btnAction.setStyle("-fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10; -fx-text-fill: white;");
                btnAction.setOnAction(event -> handleAction(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderResponseDTO order = getTableView().getItems().get(getIndex());
                    String status = order.getStatus().toUpperCase();
                    boolean isAdmin = UserSession.getInstance().isAdminOrStaff();

                    if (isAdmin) {
                        // Admin/Staff: Duyệt (PENDING->SHIPPING) hoặc Hoàn tất (SHIPPING->COMPLETED)
                        if ("PENDING".equals(status) || "SHIPPING".equals(status)) {
                            btnAction.setText("PENDING".equals(status) ? "✓ Duyệt" : "✓ Hoàn tất");
                            btnAction.setStyle("-fx-background-color: " + ("PENDING".equals(status) ? "#27ae60" : "#2980b9") + "; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10; -fx-text-fill: white;");
                            setGraphic(btnAction);
                        } else setGraphic(null);
                    } else {
                        // Customer: Chỉ được Hủy khi PENDING
                        if ("PENDING".equals(status)) {
                            btnAction.setText("✕ Hủy đơn");
                            btnAction.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10; -fx-text-fill: white;");
                            setGraphic(btnAction);
                        } else setGraphic(null);
                    }
                }
            }
        });
    }

    private void handleAction(OrderResponseDTO order) {
        if (UserSession.getInstance().isAdminOrStaff()) {
            String nextStatus = "PENDING".equalsIgnoreCase(order.getStatus()) ? "SHIPPING" : "COMPLETED";
            updateStatusAPI(order.getId(), nextStatus);
        } else {
            cancelOrderAPI(order.getId());
        }
    }

    // Sửa hàm updateStatusAPI và cancelOrderAPI
    private void updateStatusAPI(Long id, String status) {
        String jsonBody = "{\"status\": \"" + status + "\"}";
        ApiClient.getInstance().patch("/orders/" + id + "/status", jsonBody).thenAccept(response -> {
            if (response.statusCode() == 200) {
                // Cập nhật lại UI sau khi thành công
                Platform.runLater(this::determineAndLoadData);
            } else {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại!").show());
            }
        });
    }

    private void determineAndLoadData() {
        // Chỉ clear khi load xong (để tránh màn hình trắng)
        if (UserSession.getInstance().isAdminOrStaff()) {
            loadOrderHistory("/orders?page=0&size=20&sortBy=orderDate&direction=desc");
        } else {
            loadOrderHistory("/orders/history?page=0&size=20");
        }
    }

    private void cancelOrderAPI(Long id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn chắc chắn muốn hủy đơn này?");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                // Gọi hàm post mới chỉ với endpoint
                ApiClient.getInstance().post("/orders/" + id + "/cancel").thenAccept(res -> {
                    if (res.statusCode() == 200) {
                        Platform.runLater(this::determineAndLoadData);
                    } else {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Hủy đơn thất bại!").show());
                    }
                });
            }
        });
    }

    @Override
    public void onNavigate(Object data) { determineAndLoadData(); }

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