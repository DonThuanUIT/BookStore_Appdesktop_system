package com.bookstore.frontend.controller;

import com.bookstore.frontend.controller.strategy.EditBookStrategy;
import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.InventoryModel;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.util.LoadingUtils;
import com.bookstore.frontend.util.PaginationSynchronizer;
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
import java.util.ArrayList;
import java.util.List;

public class InventoryController extends BaseController {

    @FXML private Label lblTotalTitles;
    @FXML private Label lblLowStock;
    @FXML private Label lblPaginationInfo;
    @FXML private Button btnFilterLowStock;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    @FXML private TableView<BookModel> tvInventory;
    @FXML private TableColumn<BookModel, Long> colId;
    @FXML private TableColumn<BookModel, String> colTitle;
    @FXML private TableColumn<BookModel, String> colAuthor;
    @FXML private TableColumn<BookModel, Integer> colQty;
    @FXML private TableColumn<BookModel, Double> colPrice;
    @FXML private TableColumn<BookModel, Void> colActions;

    private InventoryModel model;
    private InventoryInteractor interactor;
    private boolean isLowStockFilterActive = false;

    // Biến phân trang
    private List<BookModel> fullInventoryList = new ArrayList<>();
    private int currentPage = 0;
    private final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        this.model = new InventoryModel();
        this.interactor = new InventoryInteractor(this.model);

        setupTableColumns();
        setupPaginationSync();  // Thêm đồng bộ hóa phân trang
        setupRealTimeSync();
        loadAndRender();
    }
    
    /**
     * Thiết lập đồng bộ hóa phân trang với các page khác
     */
    private void setupPaginationSync() {
        PaginationSynchronizer.getInstance().addListener((pageType, page, pageSize) -> {
            if ("INVENTORY".equals(pageType)) {
                currentPage = page;
                renderPage(page);
            }
        });
    }

    private void loadAndRender() {
        System.out.println("[InventoryController.loadAndRender] Bắt đầu fetch sách...");
        interactor.fetchAllBooks().thenAccept(list -> {
            System.out.println("[InventoryController.loadAndRender] Nhận được " + list.size() + " sách từ API");
            this.fullInventoryList = list;
            Platform.runLater(() -> {
                System.out.println("[InventoryController.loadAndRender] Render page 0...");
                renderPage(0);
            });
        }).exceptionally(ex -> {
            System.err.println("[InventoryController.loadAndRender] Exception: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }

    private void renderPage(int page) {
        this.currentPage = page;
        List<BookModel> sourceList = isLowStockFilterActive
                ? fullInventoryList.stream().filter(b -> b.getQuantity() < 10).toList()
                : fullInventoryList;

        int from = Math.min(page * PAGE_SIZE, sourceList.size());
        int to = Math.min(from + PAGE_SIZE, sourceList.size());

        model.getBooks().setAll(sourceList.subList(from, to));
        int totalPages = (int) Math.ceil((double) sourceList.size() / PAGE_SIZE);
        model.setPaginationInfo("Trang " + (page + 1) + "/" + Math.max(1, totalPages));
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= totalPages - 1 || totalPages == 0);
    }

    @FXML private void handleNextPage() { 
        renderPage(currentPage + 1);
        PaginationSynchronizer.getInstance().setInventoryPage(currentPage + 1, PAGE_SIZE);
    }
    
    @FXML private void handlePrevPage() { 
        if(currentPage > 0) {
            renderPage(currentPage - 1);
            PaginationSynchronizer.getInstance().setInventoryPage(currentPage - 1, PAGE_SIZE);
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("formattedAuthors"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        String centerHeader = "-fx-alignment: center; -fx-text-fill: #AAAAAA; -fx-font-weight: bold;";
        String leftHeader = "-fx-alignment: center-left; -fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-padding: 0 0 0 10;";

        Label idHeader = new Label("MÃ"); idHeader.setStyle(centerHeader); idHeader.setMaxWidth(Double.MAX_VALUE); colId.setGraphic(idHeader); colId.setText("");
        Label titleHeader = new Label("TÊN SÁCH"); titleHeader.setStyle(leftHeader); titleHeader.setMaxWidth(Double.MAX_VALUE); colTitle.setGraphic(titleHeader); colTitle.setText("");
        Label authorHeader = new Label("TÁC GIẢ"); authorHeader.setStyle(leftHeader); authorHeader.setMaxWidth(Double.MAX_VALUE); colAuthor.setGraphic(authorHeader); colAuthor.setText("");
        Label qtyHeader = new Label("TỒN KHO"); qtyHeader.setStyle(centerHeader); qtyHeader.setMaxWidth(Double.MAX_VALUE); colQty.setGraphic(qtyHeader); colQty.setText("");
        Label priceHeader = new Label("GIÁ BÁN"); priceHeader.setStyle(centerHeader); priceHeader.setMaxWidth(Double.MAX_VALUE); colPrice.setGraphic(priceHeader); colPrice.setText("");
        Label actionHeader = new Label("THAO TÁC"); actionHeader.setStyle(centerHeader); actionHeader.setMaxWidth(Double.MAX_VALUE); colActions.setGraphic(actionHeader); colActions.setText("");

        colQty.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();
            private final HBox container = new HBox(badge);
            { container.setAlignment(javafx.geometry.Pos.CENTER); }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    badge.setText(item + " CUỐN");
                    badge.setStyle(item < 10 ? "-fx-text-fill: #ff5555; -fx-background-color: rgba(255,85,85,0.15); -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-weight: bold;" : "-fx-text-fill: #AAAAAA; -fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12; -fx-padding: 4 12;");
                    setGraphic(container);
                }
            }
        });

        // FIX: Định dạng cột giá bán theo tiền tệ Việt Nam (VND)
        colPrice.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(com.bookstore.frontend.util.CurrencyUtils.formatVND(price));
                    setStyle("-fx-text-fill: -fx-accent-gold; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(15, btnEdit, btnDelete);
            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-font-size: 16px;");
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-font-size: 16px;");
                btnEdit.setOnAction(event -> onEditBook(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> onDeleteBook(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

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
            dialogStage.setScene(new Scene(page));
            BookFormController controller = loader.getController();
            controller.setBook(selectedBook, new EditBookStrategy(this.interactor));
            dialogStage.showAndWait();
            if (controller.isSaveClicked()) loadAndRender();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void onDeleteBook(BookModel book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa sách: " + book.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            interactor.deleteBook(book.getId()).thenAccept(success -> Platform.runLater(this::loadAndRender));
        }
    }

    @FXML public void handleFilterLowStock() {
        isLowStockFilterActive = !isLowStockFilterActive;
        renderPage(0);
    }

    @Override public void onNavigate(Object data) { loadAndRender(); }

    private void setupRealTimeSync() {
        ApiClient.getInstance().onBookUpdated(updatedBook -> Platform.runLater(this::loadAndRender));
        ApiClient.getInstance().onBookDeleted(bookId -> Platform.runLater(this::loadAndRender));
    }
}