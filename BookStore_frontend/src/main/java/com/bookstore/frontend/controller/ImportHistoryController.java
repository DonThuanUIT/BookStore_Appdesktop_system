package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.model.ImportModel;
import com.bookstore.frontend.navigation.Navigatable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    @FXML private TableColumn<ImportModel, String> colStaff;
    @FXML private TableColumn<ImportModel, String> colDate;
    @FXML private TableColumn<ImportModel, Double> colTotal;
    @FXML private TableColumn<ImportModel, Void> colActions;

    @FXML private VBox importDetailSidePanel;
    @FXML private ImportDetailSidePanelController importDetailSidePanelController;

    private ImportManagementModel model;
    private ImportInteractor interactor;

    @FXML
    public void initialize() {
        this.model = new ImportManagementModel();
        this.interactor = new ImportInteractor(this.model);

        setupTableColumns();
        setupFilters();
        loadPage(0);
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

        tvImports.setItems(model.getImports());
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
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> loadPage(0));
    }

    @FXML
    private void handleFilter() {
        loadPage(0);
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
        String keyword = txtSearch.getText();
        interactor.loadImportHistory(page, PAGE_SIZE, keyword);
    }

    @FXML
    private void handleCreateImport() {
        com.bookstore.frontend.navigation.NavigationService.getInstance()
                .navigateTo(com.bookstore.frontend.navigation.PageType.IMPORT_CREATE);
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

    @Override
    public void onNavigate(Object data) {
        loadPage(0);
    }

    private void onDeleteImport(ImportModel importRecord) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc chắn muốn xóa phiếu nhập #IMP-" + importRecord.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            interactor.deleteImport(importRecord.getId()).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        int page = model.getCurrentPage();
                        if (page > 0 && model.getImports().size() <= 1) {
                            page--;
                        }
                        loadPage(page);
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Xóa thất bại!").show();
                    }
                });
            });
        }
    }
}
