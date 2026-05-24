package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.InventoryModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InventoryController extends BaseController {

    @FXML private Label lblTotalTitles;
    @FXML private Label lblLowStock;
    @FXML private Label lblPaginationInfo;

    @FXML private TableView<BookModel> tvInventory;
    @FXML private TableColumn<BookModel, Long> colId;
    @FXML private TableColumn<BookModel, String> colTitle;
    @FXML private TableColumn<BookModel, String> colAuthor;
    @FXML private TableColumn<BookModel, Integer> colQty;
    @FXML private TableColumn<BookModel, Double> colPrice;
    @FXML private TableColumn<BookModel, Void> colActions;

    private InventoryModel model;
    private InventoryInteractor interactor;

    @FXML
    public void initialize() {
        this.model = new InventoryModel();
        this.interactor = new InventoryInteractor(this.model);

        setupTableColumns();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("formattedAuthors"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        colQty.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + " UNITS");
                    if (item < 10) {
                        setStyle("-fx-text-fill: #ff5555; -fx-background-color: rgba(255,85,85,0.1); -fx-background-radius: 10; -fx-padding: 2 10; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #AAAAAA; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-padding: 2 10; -fx-alignment: center;");
                    }
                }
            }
        });

        // EDIT VÀ DELETE (ACTIONS)
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-font-size: 16px;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-font-size: 16px;");

                btnEdit.setOnAction(event -> {
                    BookModel book = getTableView().getItems().get(getIndex());
                    onEditBook(book);
                });

                btnDelete.setOnAction(event -> {
                    BookModel book = getTableView().getItems().get(getIndex());
                    onDeleteBook(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Bind Data
        tvInventory.setItems(model.getBooks());
        lblTotalTitles.textProperty().bind(model.totalTitlesProperty().asString());
        lblLowStock.textProperty().bind(model.lowStockCountProperty().asString());
        lblPaginationInfo.textProperty().bind(model.paginationInfoProperty());
    }

    private void onEditBook(BookModel selectedBook) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/BookFormView.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();

            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(page);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);

            BookFormController controller = loader.getController();
            controller.setBook(selectedBook, true);

            controller.setInteractor(this.interactor);

            dialogStage.showAndWait();


            if (controller.isSaveClicked()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cập nhật sách thành công!");
                alert.show();
                interactor.loadInventoryData(0, 15);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onDeleteBook(BookModel book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa sách: " + book.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            interactor.deleteBook(book.getId()).thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        interactor.loadInventoryData(0, 15);
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa sách!");
                        err.show();
                    }
                });
            });
        }
    }

    @FXML
    public void handleFilterLowStock() {
        System.out.println("Filtering books that are out of stock...");
        // TODO: Gọi API lấy sách quantity < 10
    }

    @Override
    public void onNavigate(Object data) {
        if (interactor != null) {
            interactor.loadInventoryData(0, 15);
        }
    }
}