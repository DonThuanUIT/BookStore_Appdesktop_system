package com.bookstore.frontend.controller;

import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.model.dto.Response.OrderResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class OrderHistoryController {
    @FXML private TableView<OrderResponseDTO> orderTable;
    @FXML private TableColumn<OrderResponseDTO, Long> colId;
    @FXML private TableColumn<OrderResponseDTO, String> colDate;
    @FXML private TableColumn<OrderResponseDTO, Double> colTotal;
    @FXML private TableColumn<OrderResponseDTO, String> colStatus;
    @FXML private TableColumn<OrderResponseDTO, String> colPayment;
    @FXML private TableColumn<OrderResponseDTO, Double> colDiscount;

    @FXML
    public void initialize() {
        // Ánh xạ các cột với getter trong DTO
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        ApiClient.getInstance().get("/orders/history?page=0&size=20").thenAccept(response -> {
            // Ghi log để chẩn đoán
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Body: " + response.body());

            if (response.statusCode() != 200) {
                System.err.println("Lỗi gọi API: " + response.statusCode());
                return;
            }

            try {
                ObjectMapper mapper = ApiClient.getInstance().getMapper();
                JsonNode root = mapper.readTree(response.body());

                // Kiểm tra xem node "content" có tồn tại không
                if (root.has("content")) {
                    List<OrderResponseDTO> orders = mapper.convertValue(root.get("content"), new TypeReference<List<OrderResponseDTO>>() {});

                    Platform.runLater(() -> {
                        orderTable.setItems(FXCollections.observableArrayList(orders));
                    });
                } else {
                    System.err.println("JSON không có trường 'content'");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).exceptionally(ex -> {
            System.err.println("Lỗi kết nối: " + ex.getMessage());
            return null;
        });
    }
}