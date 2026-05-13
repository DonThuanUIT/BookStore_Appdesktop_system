package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.model.ImportModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    @FXML
    public void initialize() {
        this.model = new ImportManagementModel();
        this.interactor = new ImportInteractor(this.model);

        setupTableColumns();

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

        tvImports.setItems(model.getImports());
        lblPaginationInfo.textProperty().bind(model.paginationInfoProperty());
    }

    @FXML
    private void handleFilter() {
        System.out.println("Đang lọc dữ liệu... Tính năng này sẽ nối API sau.");
    }

    @FXML
    private void handleCreateImport() {
        // Điều hướng sang màn hình Import Create (Bạn cần thêm IMPORT_CREATE vào enum PageType nhé)
        com.bookstore.frontend.navigation.NavigationService.getInstance().navigateTo(com.bookstore.frontend.navigation.PageType.IMPORT_CREATE);
    }

    // --- LOGIC GỌI API VÀ MỞ SIDE PANEL ---
    private void onViewDetails(ImportModel importRecord) {
        System.out.println("Đang tải dữ liệu chi tiết phiếu #" + importRecord.getId() + " từ Backend...");

        interactor.getImportDetails(importRecord.getId()).thenAccept(fullImportData -> {
            Platform.runLater(() -> {
                if (fullImportData != null) {
                    // Truyền dữ liệu sang Side Panel và yêu cầu trượt ra
                    importDetailSidePanelController.setImportDataAndShow(fullImportData);
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Không thể tải chi tiết phiếu nhập!");
                    err.show();
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
                        Alert err = new Alert(Alert.AlertType.ERROR, "Xóa thất bại!");
                        err.show();
                    }
                });
            });
        }
    }
}