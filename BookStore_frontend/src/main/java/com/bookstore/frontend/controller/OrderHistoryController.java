package com.bookstore.frontend.controller;

import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.model.dto.Response.OrderResponseDTO;
import com.bookstore.frontend.navigation.Navigatable;
import com.bookstore.frontend.util.LoadingUtils;
import com.bookstore.frontend.util.OrderStatusStore;
import com.bookstore.frontend.util.PaginationSynchronizer;
import com.bookstore.frontend.util.PaginationUtil;
import com.bookstore.frontend.util.UserSession;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderHistoryController implements Navigatable {
    
    @FXML private TableView<OrderResponseDTO> orderTable;
    @FXML private TableColumn<OrderResponseDTO, Long> colId;
    @FXML private TableColumn<OrderResponseDTO, String> colDate;
    @FXML private TableColumn<OrderResponseDTO, Double> colTotal;
    @FXML private TableColumn<OrderResponseDTO, String> colStatus;
    @FXML private TableColumn<OrderResponseDTO, String> colPayment;
    @FXML private TableColumn<OrderResponseDTO, Void> colAction;
    @FXML private Label lblTitle;
    @FXML private Label lblPaginationInfo;
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;
    @FXML private VBox paginationControls;
    
    // Phân trang
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10; // Cố định 10 items/trang để load nhanh
    private List<OrderResponseDTO> allOrders = new ArrayList<>();
    private int totalOrders = 0;
    
    // Theo dõi loại người dùng để load dữ liệu đúng endpoint
    private boolean isAdminView = false;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        setupActionColumn();
        setupPaginationControls();
        
        // Đăng ký listener cho PaginationSynchronizer
        registerPaginationSync();
        
        determineAndLoadData();
    }
    
    /**
     * Đăng ký đồng bộ hóa phân trang
     */
    private void registerPaginationSync() {
        PaginationSynchronizer.getInstance().addListener((pageType, page, pageSize) -> {
            if ("ORDER_HISTORY".equals(pageType)) {
                currentPage = page;
                loadOrderHistoryForPage();
            }
        });
    }
    
    /**
     * Thiết lập các nút điều khiển phân trang
     */
    private void setupPaginationControls() {
        if (btnPrevPage != null) {
            btnPrevPage.setOnAction(e -> handlePrevPage());
        }
        if (btnNextPage != null) {
            btnNextPage.setOnAction(e -> handleNextPage());
        }
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnAction = new Button();
            {
                btnAction.setStyle("-fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10; -fx-text-fill: white;");
                btnAction.setOnAction(event -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        handleAction(getTableView().getItems().get(idx));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    OrderResponseDTO order = getTableView().getItems().get(getIndex());
                    String status = order.getStatus().toUpperCase();

                    boolean isAdmin = UserSession.getInstance().isAdmin();

                    if (isAdmin) {
                        if ("PENDING".equals(status) || "SHIPPING".equals(status)) {
                            btnAction.setText("PENDING".equals(status) ? "✓ Duyệt" : "✓ Hoàn tất");
                            btnAction.setStyle("-fx-background-color: " + ("PENDING".equals(status) ? "#27ae60" : "#2980b9") + "; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10; -fx-text-fill: white;");
                            setGraphic(btnAction);
                        } else setGraphic(null);
                    } else {
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
        if (UserSession.getInstance().isAdmin()) {
            String nextStatus = "PENDING".equalsIgnoreCase(order.getStatus()) ? "SHIPPING" : "COMPLETED";
            updateStatusAPI(order.getId(), nextStatus);
        } else {
            cancelOrderAPI(order.getId());
        }
    }

    private void updateStatusAPI(Long id, String status) {
        try {
            String jsonBody = "{\"status\":\"" + status + "\"}";

            // Gửi request
            ApiClient.getInstance().patchRaw("/orders/" + id + "/status", jsonBody).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        // Kiểm tra nếu đơn hàng vừa chuyển sang trạng thái xử lý/giao hàng
                        // thì giảm số lượng đơn chờ ở icon User
                        if ("SHIPPING".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                            OrderStatusStore.getInstance().decrementPendingOrder();
                        }
                        // Tải lại dữ liệu trang hiện tại
                        loadOrderHistoryForPage();
                    } else {
                        System.err.println("Lỗi Backend: " + response.body());
                        new Alert(Alert.AlertType.ERROR, "Lỗi cập nhật: " + response.body()).show();
                    }
                });
            });
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void determineAndLoadData() {
        currentPage = 0;
        isAdminView = UserSession.getInstance().isAdmin();
        loadOrderHistoryForPage();
    }

    private void cancelOrderAPI(Long id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn chắc chắn muốn hủy đơn này?");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                LoadingUtils.show("Đang hủy đơn...");
                ApiClient.getInstance().post("/orders/" + id + "/cancel").thenAccept(res -> {
                    Platform.runLater(() -> {
                        LoadingUtils.hide();
                        if (res.statusCode() == 200) {
                            // Tải lại dữ liệu
                            loadOrderHistoryForPage();
                        } else {
                            new Alert(Alert.AlertType.ERROR, "Hủy đơn thất bại!").show();
                        }
                    });
                });
            }
        });
    }

    @Override
    public void onNavigate(Object data) { 
        determineAndLoadData(); 
    }

    /**
     * Tải dữ liệu đơn hàng cho trang hiện tại
     */
    private void loadOrderHistoryForPage() {
        LoadingUtils.show("Đang tải đơn hàng...");
        
        String endpoint;
        if (isAdminView) {
            endpoint = String.format("/orders?page=%d&size=10&sortBy=orderDate&direction=desc", 
                    currentPage);
        } else {
            endpoint = String.format("/orders/history?page=%d&size=10", 
                    currentPage);
        }
        
        ApiClient.getInstance().get(endpoint).thenAccept(response -> {
            Platform.runLater(() -> {
                LoadingUtils.hide();
                if (response.statusCode() == 200) {
                    try {
                        ObjectMapper mapper = ApiClient.getInstance().getMapper();
                        JsonNode root = mapper.readTree(response.body());
                        
                        // Lấy danh sách orders
                        List<OrderResponseDTO> orders = mapper.convertValue(
                            root.get("content"), 
                            new TypeReference<List<OrderResponseDTO>>() {}
                        );
                        
                        // Lấy tổng số trang từ response
                        int totalPages = root.has("totalPages") ? root.get("totalPages").asInt() : 1;
                        totalOrders = root.has("totalElements") ? root.get("totalElements").asInt() : 0;
                        
                        // Cập nhật table
                        orderTable.setItems(FXCollections.observableArrayList(orders));
                        
                        // Cập nhật UI phân trang
                        updatePaginationUI(totalPages);
                        
                        // Đồng bộ hóa với các page khác
                        PaginationSynchronizer.getInstance().setImportHistoryPage(currentPage, PAGE_SIZE);
                        
                    } catch (Exception e) { 
                        e.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Lỗi parse dữ liệu").show();
                    }
                } else {
                    new Alert(Alert.AlertType.ERROR, "Lỗi tải dữ liệu").show();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                LoadingUtils.hide();
                System.err.println("Exception loading orders: " + ex.getMessage());
                ex.printStackTrace();
            });
            return null;
        });
    }
    
    /**
     * Cập nhật UI phân trang
     */
    private void updatePaginationUI(int totalPages) {
        // Cập nhật label thông tin phân trang
        if (lblPaginationInfo != null) {
            int from = PaginationUtil.getFromIndex(currentPage, PAGE_SIZE) + 1;
            int to = Math.min((currentPage + 1) * PAGE_SIZE, totalOrders);
            lblPaginationInfo.setText(String.format("Trang %d/%d (%d-%d của %d)",
                    currentPage + 1,
                    Math.max(1, totalPages),
                    totalOrders > 0 ? from : 0,
                    to,
                    totalOrders));
        }
        
        // Disable/enable nút Previous
        if (btnPrevPage != null) {
            btnPrevPage.setDisable(currentPage <= 0);
        }
        
        // Disable/enable nút Next
        if (btnNextPage != null) {
            btnNextPage.setDisable(currentPage >= totalPages - 1 || totalPages <= 1);
        }
    }
    
    /**
     * Xử lý nút Previous
     */
    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadOrderHistoryForPage();
        }
    }
    
    /**
     * Xử lý nút Next
     */
    @FXML
    private void handleNextPage() {
        currentPage++;
        loadOrderHistoryForPage();
    }
}