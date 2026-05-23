package com.bookstore.frontend.controller;

import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.model.dto.Response.OrderResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class OrderHistoryController {
    @FXML private TableView<OrderResponseDTO> orderTable;
    @FXML private TableColumn<OrderResponseDTO, Long> colId;
    @FXML private TableColumn<OrderResponseDTO, String> colStatus;
    @FXML private TableColumn<OrderResponseDTO, Double> colTotal;

    @FXML
    public void initialize() {
        // Cấu hình các cột trong TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        ApiClient.getInstance().get("/orders/history?page=0&size=20").thenAccept(response -> {
            try {
                // Backend trả về dạng Page, nên chúng ta cần lấy trường "content"
                ObjectMapper mapper = ApiClient.getInstance().getMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode content = root.get("content");

                List<OrderResponseDTO> orders = mapper.convertValue(content, new TypeReference<>() {});

                Platform.runLater(() -> {
                    ObservableList<OrderResponseDTO> list = FXCollections.observableArrayList(orders);
                    orderTable.setItems(list);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}