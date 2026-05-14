package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.model.ImportModel;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class ImportHistoryController {

    @FXML private TextField txtSearch;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private Label lblPaginationInfo;

    @FXML private TableView<ImportModel> tvImports;
    @FXML private TableColumn<ImportModel, Long> colId;
    @FXML private TableColumn<ImportModel, String> colStaff;
    @FXML private TableColumn<ImportModel, String> colDate;
    @FXML private TableColumn<ImportModel, Double> colTotal;
    @FXML private TableColumn<ImportModel, Void> colActions;

    // --- INJECT SIDE PANEL COMPONENTS ---
    @FXML private VBox importDetailSidePanel;
    @FXML private ImportDetailSidePanelController importDetailSidePanelController;

    private ImportManagementModel model;
    private ImportInteractor interactor;

    // Lớp màng lọc dữ liệu
    private FilteredList<ImportModel> filteredData;

    @FXML
    public void initialize() {
        this.model = new ImportManagementModel();
        this.interactor = new ImportInteractor(this.model);

        setupTableColumns();
        setupFilters();

        // Load dữ liệu ngay khi vừa mở màn hình
        interactor.loadImportHistory();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStaff.setCellValueFactory(new PropertyValueFactory<>("staffUsername"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("importDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        colId.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#IMP-" + item);
                    setStyle("-fx-text-fill: #AAAAAA;");
                }
            }
        });

        colTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                    setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(15, btnView, btnDelete);

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                btnView.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFC107; -fx-cursor: hand; -fx-font-size: 16px;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-cursor: hand; -fx-font-size: 16px;");

                btnView.setOnAction(event -> {
                    ImportModel importRecord = getTableView().getItems().get(getIndex());
                    onViewDetails(importRecord);
                });

                btnDelete.setOnAction(event -> {
                    ImportModel importRecord = getTableView().getItems().get(getIndex());
                    onDeleteImport(importRecord);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        lblPaginationInfo.textProperty().bind(model.paginationInfoProperty());
    }

    // --- LOGIC TÌM KIẾM VÀ LỌC (FILTER) ---
    private void setupFilters() {
        // Bọc danh sách gốc vào FilteredList
        filteredData = new FilteredList<>(model.getImports(), p -> true);

        // Đổ danh sách ĐÃ LỌC vào TableView
        tvImports.setItems(filteredData);

        // Bắt sự kiện gõ phím để tìm kiếm Real-time
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    @FXML
    private void handleFilter() {
        // Khi nhấn nút Lọc (thường dùng cho ngày tháng)
        applyFilters();
    }

    private void applyFilters() {
        String searchText = txtSearch.getText() != null ? txtSearch.getText().toLowerCase().trim() : "";
        LocalDate fromDate = dpFrom.getValue();
        LocalDate toDate = dpTo.getValue();

        filteredData.setPredicate(importRecord -> {
            // 1. Kiểm tra khớp Text (Mã phiếu hoặc Tên nhân viên)
            boolean matchesSearch = true;
            if (!searchText.isEmpty()) {
                String idStr = String.valueOf(importRecord.getId());
                // Xóa chữ "IMP-" hoặc "#IMP-" nếu user lỡ gõ vào
                String cleanSearchText = searchText.replace("#", "").replace("imp-", "");

                String staff = importRecord.getStaffUsername() != null ? importRecord.getStaffUsername().toLowerCase() : "";
                matchesSearch = idStr.contains(cleanSearchText) || staff.contains(searchText);
            }

            // 2. Kiểm tra khớp Ngày tháng (Tạm thời bỏ qua vì Backend đang trả về "N/A")
            boolean matchesDate = true;
            /* * TODO: Khi Backend làm xong Ngày nhập, mở đoạn code này ra:
             * if (!"N/A".equals(importRecord.getImportDate())) {
             * LocalDate recordDate = LocalDate.parse(importRecord.getImportDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
             * if (fromDate != null && recordDate.isBefore(fromDate)) matchesDate = false;
             * if (toDate != null && recordDate.isAfter(toDate)) matchesDate = false;
             * }
             */

            return matchesSearch && matchesDate;
        });

        // Cập nhật lại nhãn đếm số lượng sau khi lọc
        lblPaginationInfo.setText("Showing " + filteredData.size() + " entries (Filtered)");
    }

    @FXML
    private void handleCreateImport() {
        com.bookstore.frontend.navigation.NavigationService.getInstance().navigateTo(com.bookstore.frontend.navigation.PageType.IMPORT_CREATE);
    }

    private void onViewDetails(ImportModel importRecord) {
        interactor.getImportDetails(importRecord.getId()).thenAccept(fullImportData -> {
            Platform.runLater(() -> {
                if (fullImportData != null) {
                    importDetailSidePanelController.setImportDataAndShow(fullImportData);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Không thể tải chi tiết phiếu nhập!").show();
                }
            });
        });
    }

    private void onDeleteImport(ImportModel importRecord) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa phiếu nhập #IMP-" + importRecord.getId() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            interactor.deleteImport(importRecord.getId()).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        interactor.loadImportHistory();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Xóa thất bại!").show();
                    }
                });
            });
        }
    }
}