package com.bookstore.frontend.controller;

import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.model.dto.Response.OrderResponseDTO;
import com.bookstore.frontend.model.dto.Response.OrderDetailResponseDTO;
import com.bookstore.frontend.navigation.Navigatable;
import com.bookstore.frontend.util.*;
import com.bookstore.frontend.utils.AlertUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryController implements Navigatable {
    @FXML private TableView<OrderResponseDTO> orderTable;
    @FXML private TableColumn<OrderResponseDTO, Long> colId;
    @FXML private TableColumn<OrderResponseDTO, LocalDateTime> colDate;
    @FXML private TableColumn<OrderResponseDTO, Double> colTotal;
    @FXML private TableColumn<OrderResponseDTO, String> colStatus;
    @FXML private TableColumn<OrderResponseDTO, String> colPayment;
    @FXML private TableColumn<OrderResponseDTO, Double> colDiscount;
    @FXML private TableColumn<OrderResponseDTO, Void> colAction;
    @FXML private Label lblTitle;

    // Các control filter mới
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    // Các control phân trang mới
    @FXML private Label lblPaginationInfo;
    @FXML private Button btnPrevPage;
    @FXML private Label lblCurrentPage;
    @FXML private Button btnNextPage;

    // Side panel chi tiết
    @FXML private VBox orderDetailSidePanel;
    @FXML private OrderDetailSidePanelController orderDetailSidePanelController;

    private int currentPage = 0;
    private int totalPages = 0;
    private final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));

        // Định dạng cột ID
        colId.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#ORD-" + item);
                    setStyle("-fx-text-fill: #AAAAAA; -fx-alignment: center;");
                }
            }
        });

        // Định dạng cột ngày tháng
        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    setStyle("-fx-text-fill: #AAAAAA; -fx-alignment: center;");
                }
            }
        });

        // Định dạng cột Tổng tiền
        colTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f đ", item));
                    setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-alignment: center-right;");
                }
            }
        });

        // Định dạng cột Giảm giá
        colDiscount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f đ", item));
                    setStyle("-fx-text-fill: #ff5555; -fx-alignment: center-right;");
                }
            }
        });

        // Định dạng cột trạng thái
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(translateStatus(item));
                    if ("PENDING".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-alignment: center;");
                    } else if ("SHIPPING".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-alignment: center;");
                    } else if ("COMPLETED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: center;");
                    }
                }
            }
        });

        // Thiết lập sự kiện double-click trên dòng TableView để mở chi tiết
        orderTable.setRowFactory(tv -> {
            TableRow<OrderResponseDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    onViewDetails(row.getItem());
                }
            });
            return row;
        });

        setupActionColumn();
        setupFilters();
        determineAndLoadData();
    }

    private void setupFilters() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "Tất cả", "Chờ duyệt", "Đang giao", "Hoàn thành", "Đã hủy"
        ));
        cbStatus.setValue("Tất cả");

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            determineAndLoadData();
        });
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            determineAndLoadData();
        });
        dpFrom.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            determineAndLoadData();
        });
        dpTo.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            determineAndLoadData();
        });

        dpFrom.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                dpFrom.setValue(null);
            }
        });
        dpTo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                dpTo.setValue(null);
            }
        });

        // Ẩn side panel khi bắt đầu
        if (orderDetailSidePanel != null) {
            orderDetailSidePanel.setVisible(false);
            orderDetailSidePanel.setManaged(false);
        }
    }

    @FXML
    private void handleFilter() {
        currentPage = 0;
        determineAndLoadData();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            determineAndLoadData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            determineAndLoadData();
        }
    }

    private void updatePaginationControls(int totalElements) {
        lblCurrentPage.setText(String.valueOf(currentPage + 1));
        btnPrevPage.setDisable(currentPage <= 0);
        btnNextPage.setDisable(currentPage >= totalPages - 1 || totalPages == 0);

        int startEntry = currentPage * PAGE_SIZE + 1;
        int endEntry = Math.min(startEntry + PAGE_SIZE - 1, totalElements);
        if (totalElements == 0) {
            lblPaginationInfo.setText("Hiển thị 0 đơn hàng");
        } else {
            lblPaginationInfo.setText(String.format("Hiển thị từ đơn thứ %d đến %d (Tổng số: %d đơn hàng)", startEntry, endEntry, totalElements));
        }
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁");
            private final Button btnAction = new Button();
            private final HBox pane = new HBox(10, btnView, btnAction);

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                btnView.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFC107; -fx-cursor: hand; -fx-font-size: 16px;");
                btnView.setOnAction(event -> {
                    OrderResponseDTO order = getTableView().getItems().get(getIndex());
                    onViewDetails(order);
                });
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
                    boolean isAdmin = UserSession.getInstance().isAdmin();

                    pane.getChildren().clear();
                    pane.getChildren().add(btnView);

                    boolean hasAction = false;
                    if (isAdmin) {
                        if ("PENDING".equals(status) || "SHIPPING".equals(status)) {
                            btnAction.setText("PENDING".equals(status) ? "Duyệt" : "Hoàn tất");
                            btnAction.setStyle("-fx-background-color: " + ("PENDING".equals(status) ? "#27ae60" : "#2980b9") + "; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 4 8; -fx-text-fill: white; -fx-font-weight: bold;");
                            hasAction = true;
                        }
                    } else {
                        if ("PENDING".equals(status)) {
                            btnAction.setText("Hủy");
                            btnAction.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 4 8; -fx-text-fill: white; -fx-font-weight: bold;");
                            hasAction = true;
                        }
                    }

                    if (hasAction) {
                        pane.getChildren().add(btnAction);
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void onViewDetails(OrderResponseDTO order) {
        Platform.runLater(() -> {
            if (orderDetailSidePanel != null) {
                orderDetailSidePanel.setManaged(true);
                orderDetailSidePanel.setVisible(true);
                orderDetailSidePanelController.setOrderDataAndShow(order);
            }
        });
    }

    private void handleAction(OrderResponseDTO order) {
        if (UserSession.getInstance().isAdmin()) {
            String status = order.getStatus().toUpperCase();
            String nextStatus = "PENDING".equals(status) ? "SHIPPING" : "COMPLETED";
            String actionLabel = "PENDING".equals(status) ? "Duyệt" : "Hoàn tất";

            if (AlertUtils.confirm("Xác nhận", "Bạn có chắc chắn muốn " + actionLabel + " đơn hàng #" + order.getId() + "?")) {
                updateStatusAPI(order.getId(), nextStatus);
            }
        } else {
            cancelOrderAPI(order.getId());
        }
    }

    private void updateStatusAPI(Long id, String status) {
        String jsonBody = "{\"status\":\"" + status + "\"}";
        ApiClient.getInstance().patchRaw("/orders/" + id + "/status", jsonBody).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    if ("SHIPPING".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                        OrderStatusStore.getInstance().decrementPendingOrder();
                    }
                    determineAndLoadData();
                    
                    // Cập nhật lại chi tiết nếu panel đang mở
                    if (orderDetailSidePanel != null && orderDetailSidePanel.isVisible()) {
                        try {
                            ObjectMapper mapper = ApiClient.getInstance().getMapper();
                            OrderResponseDTO updatedOrder = mapper.readValue(response.body(), OrderResponseDTO.class);
                            orderDetailSidePanelController.setOrderDataAndShow(updatedOrder);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                } else {
                    AlertUtils.show(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại: " + response.body());
                }
            });
        });
    }

    private void cancelOrderAPI(Long id) {
        if (AlertUtils.confirm("Xác nhận hủy", "Bạn chắc chắn muốn hủy đơn hàng này?")) {
            ApiClient.getInstance().post("/orders/" + id + "/cancel").thenAccept(res -> {
                Platform.runLater(() -> {
                    if (res.statusCode() == 200) {
                        determineAndLoadData();
                        
                        // Cập nhật lại chi tiết nếu panel đang mở
                        if (orderDetailSidePanel != null && orderDetailSidePanel.isVisible()) {
                            try {
                                ObjectMapper mapper = ApiClient.getInstance().getMapper();
                                OrderResponseDTO updatedOrder = mapper.readValue(res.body(), OrderResponseDTO.class);
                                orderDetailSidePanelController.setOrderDataAndShow(updatedOrder);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    } else {
                        AlertUtils.show(Alert.AlertType.ERROR, "Lỗi", "Hủy đơn thất bại!");
                    }
                });
            });
        }
    }

    private void determineAndLoadData() {
        StringBuilder query = new StringBuilder();
        query.append("?page=").append(currentPage).append("&size=").append(PAGE_SIZE);

        String search = txtSearch != null ? txtSearch.getText() : null;
        if (search != null && !search.trim().isEmpty()) {
            try {
                query.append("&search=").append(java.net.URLEncoder.encode(search.trim(), "UTF-8"));
            } catch (Exception e) { e.printStackTrace(); }
        }

        String statusVal = cbStatus != null ? cbStatus.getValue() : null;
        if (statusVal != null && !"Tất cả".equals(statusVal)) {
            String apiStatus = "";
            switch (statusVal) {
                case "Chờ duyệt": apiStatus = "PENDING"; break;
                case "Đang giao": apiStatus = "SHIPPING"; break;
                case "Hoàn thành": apiStatus = "COMPLETED"; break;
                case "Đã hủy": apiStatus = "CANCELED"; break;
            }
            if (!apiStatus.isEmpty()) {
                query.append("&status=").append(apiStatus);
            }
        }

        LocalDate fromDate = dpFrom != null ? dpFrom.getValue() : null;
        if (fromDate != null) {
            query.append("&startDate=").append(fromDate);
        }

        LocalDate toDate = dpTo != null ? dpTo.getValue() : null;
        if (toDate != null) {
            query.append("&endDate=").append(toDate);
        }

        if (UserSession.getInstance().isAdmin()) {
            query.append("&sortBy=orderDate&direction=desc");
            loadOrderHistory("/orders" + query.toString());
        } else {
            loadOrderHistory("/orders/history" + query.toString());
        }
    }

    @Override
    public void onNavigate(Object data) { 
        determineAndLoadData(); 
    }

    private void loadOrderHistory(String endpoint) {
        ApiClient.getInstance().get(endpoint).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    ObjectMapper mapper = ApiClient.getInstance().getMapper();
                    JsonNode root = mapper.readTree(response.body());
                    List<OrderResponseDTO> orders = mapper.convertValue(root.get("content"), new TypeReference<List<OrderResponseDTO>>() {});
                    
                    int totalElements = root.has("totalElements") ? root.get("totalElements").asInt() : orders.size();
                    totalPages = root.has("totalPages") ? root.get("totalPages").asInt() : 1;

                    Platform.runLater(() -> {
                        orderTable.setItems(FXCollections.observableArrayList(orders));
                        updatePaginationControls(totalElements);
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
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