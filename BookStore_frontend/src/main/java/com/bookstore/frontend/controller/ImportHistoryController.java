package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.model.ImportModel;
import com.bookstore.frontend.navigation.Navigatable;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.service.api.ApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ImportHistoryController implements Navigatable {

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;

    @FXML private TextField txtSearch;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private Label lblPaginationInfo;
    @FXML private Button btnPrevPage;
    @FXML private Label lblCurrentPage;
    @FXML private Button btnNextPage;

    @FXML private TableView<ImportModel> tvImports;
    @FXML private TableColumn<ImportModel, Long> colId;
    @FXML private TableColumn<ImportModel, String> colDate;
    @FXML private TableColumn<ImportModel, Double> colTotal;
    @FXML private TableColumn<ImportModel, Void> colActions;

    @FXML private VBox importDetailSidePanel;
    @FXML private ImportDetailSidePanelController importDetailSidePanelController;

    private ImportManagementModel model;
    private ImportInteractor interactor;
    private FilteredList<ImportModel> filteredData;

    @FXML
    public void initialize() {
        this.model = new ImportManagementModel();
        this.interactor = new ImportInteractor(this.model);

        setupTableColumns();
        setupFilters();
        setupRealTimeSync();
        loadPage(0);
    }

    private void setupRealTimeSync() {
        ApiClient.getInstance().onImportCreated(newImport -> {
            Platform.runLater(() -> loadPage(0));
        });

        ApiClient.getInstance().onImportDeleted(importId -> {
            Platform.runLater(() -> {
                if (importDetailSidePanel != null && importDetailSidePanel.isVisible()) {
                    importDetailSidePanel.setVisible(false);
                    importDetailSidePanel.setManaged(false);
                }
                loadPage(0);
            });
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
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
                    // FIX: Sử dụng CurrencyUtils để hiển thị VND thống nhất
                    setText(com.bookstore.frontend.util.CurrencyUtils.formatVND(item));
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

                btnView.setOnAction(event -> onViewDetails(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> onDeleteImport(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        filteredData = new FilteredList<>(model.getImports(), p -> true);

        // Listener để cập nhật UI khi dữ liệu được load từ API
        model.getImports().addListener((javafx.collections.ListChangeListener<ImportModel>) c -> {
            updateTableView();
        });

        // Listener để reset trang khi thay đổi điều kiện lọc
        txtSearch.textProperty().addListener((obs, old, newVal) -> { currentPage = 0; applyFilters(); });
        dpFrom.valueProperty().addListener((obs, old, newVal) -> { currentPage = 0; applyFilters(); });
        dpTo.valueProperty().addListener((obs, old, newVal) -> { currentPage = 0; applyFilters(); });
    }

    private void applyFilters() {
        LocalDate fromDate = dpFrom.getValue();
        LocalDate toDate = dpTo.getValue();
        String keyword = txtSearch.getText().toLowerCase();

        filteredData.setPredicate(importRecord -> {
            boolean matchesKeyword = keyword.isEmpty() || String.valueOf(importRecord.getId()).contains(keyword);

            if (fromDate == null && toDate == null) return matchesKeyword;

            String recordDateStr = importRecord.getImportDate();
            if (recordDateStr == null || "N/A".equals(recordDateStr)) return false;

            try {
                String datePart = recordDateStr.split(" ")[0];
                LocalDate recordDate = LocalDate.parse(datePart, DateTimeFormatter.ofPattern(datePart.contains("-") ? "yyyy-MM-dd" : "dd/MM/yyyy"));
                boolean matchesDate = (fromDate == null || !recordDate.isBefore(fromDate)) &&
                        (toDate == null || !recordDate.isAfter(toDate));
                return matchesKeyword && matchesDate;
            } catch (Exception e) { return false; }
        });

        updateTableView();
    }

    private void updateTableView() {
        int from = Math.min(currentPage * PAGE_SIZE, filteredData.size());
        int to = Math.min(from + PAGE_SIZE, filteredData.size());

        tvImports.setItems(FXCollections.observableArrayList(filteredData.subList(from, to)));

        int totalPages = (int) Math.ceil((double) filteredData.size() / PAGE_SIZE);
        lblCurrentPage.setText(String.valueOf(currentPage + 1));
        btnPrevPage.setDisable(currentPage <= 0);
        btnNextPage.setDisable((currentPage + 1) >= totalPages || totalPages == 0);
    }

    @FXML private void handlePrevPage() { if (currentPage > 0) { currentPage--; updateTableView(); } }
    @FXML private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) filteredData.size() / PAGE_SIZE);
        if (currentPage < totalPages - 1) { currentPage++; updateTableView(); }
    }

    private void loadPage(int page) {
        // Reset về trang đầu và load danh sách đầy đủ từ Interactor vào model
        currentPage = 0;
        interactor.loadImportHistory(0, 100, null);
    }

    @FXML private void handleFilter() { applyFilters(); }

    @FXML private void handleCreateImport() {
        com.bookstore.frontend.navigation.NavigationService.getInstance().navigateTo(PageType.IMPORT_CREATE);
    }

    @Override public void onNavigate(Object data) { loadPage(0); }

    private void onViewDetails(ImportModel importRecord) {
        interactor.getImportDetails(importRecord.getId()).thenAccept(fullImportData -> {
            Platform.runLater(() -> {
                if (fullImportData != null) importDetailSidePanelController.setImportDataAndShow(fullImportData);
                else new Alert(Alert.AlertType.ERROR, "Không thể tải chi tiết phiếu nhập!").show();
            });
        });
    }

    private void onDeleteImport(ImportModel importRecord) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa phiếu nhập #IMP-" + importRecord.getId() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            interactor.deleteImport(importRecord.getId()).thenAccept(success -> {
                Platform.runLater(() -> { if (!success) new Alert(Alert.AlertType.ERROR, "Xóa thất bại!").show(); });
            });
        }
    }
}