package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.model.ImportModel;
import com.bookstore.frontend.navigation.Navigatable;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.service.api.ApiClient;
import javafx.application.Platform;
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
                int page = model.getCurrentPage();
                if (page > 0 && model.getImports().size() <= 1) {
                    page--;
                }
                loadPage(page);
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
                    setText(String.format("%,.0f đ", item));
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
        btnPrevPage.disableProperty().bind(model.hasPreviousProperty().not());
        btnNextPage.disableProperty().bind(model.hasNextProperty().not());
        lblCurrentPage.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> String.valueOf(model.getCurrentPage() + 1),
                        model.currentPageProperty()
                )
        );
    }

    private void setupFilters() {
        filteredData = new FilteredList<>(model.getImports(), p -> true);
        tvImports.setItems(filteredData);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> loadPage(0));
        dpFrom.valueProperty().addListener((observable, oldValue, newValue) -> applyDateFilter());
        dpTo.valueProperty().addListener((observable, oldValue, newValue) -> applyDateFilter());

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
    }

    @FXML
    private void handleFilter() {
        applyDateFilter();
    }

    @FXML
    private void handlePrevPage() {
        if (model.getCurrentPage() > 0) {
            loadPage(model.getCurrentPage() - 1);
        }
    }

    @FXML
    private void handleNextPage() {
        if (model.hasNextProperty().get()) {
            loadPage(model.getCurrentPage() + 1);
        }
    }

    private void loadPage(int page) {
        String keyword = txtSearch != null ? txtSearch.getText() : null;
        interactor.loadImportHistory(page, PAGE_SIZE, keyword);
    }

    private void applyDateFilter() {
        LocalDate fromDate = dpFrom.getValue();
        LocalDate toDate = dpTo.getValue();

        filteredData.setPredicate(importRecord -> {
            if (fromDate == null && toDate == null) {
                return true;
            }
            String recordDateStr = importRecord.getImportDate();
            if (recordDateStr == null || "N/A".equals(recordDateStr)) {
                return false;
            }
            try {
                String datePart = recordDateStr.split(" ")[0];
                LocalDate recordDate;
                if (datePart.contains("-")) {
                    recordDate = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    recordDate = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                if (fromDate != null && recordDate.isBefore(fromDate)) {
                    return false;
                }
                if (toDate != null && recordDate.isAfter(toDate)) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @FXML
    private void handleCreateImport() {
        com.bookstore.frontend.navigation.NavigationService.getInstance().navigateTo(PageType.IMPORT_CREATE);
    }

    @Override
    public void onNavigate(Object data) {
        loadPage(0);
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc chắn muốn xóa phiếu nhập #IMP-" + importRecord.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            interactor.deleteImport(importRecord.getId()).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (!success) {
                        new Alert(Alert.AlertType.ERROR, "Xóa thất bại!").show();
                    }
                });
            });
        }
    }
}
