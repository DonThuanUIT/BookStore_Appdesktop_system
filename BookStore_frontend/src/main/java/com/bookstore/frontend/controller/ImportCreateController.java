package com.bookstore.frontend.controller;

import com.bookstore.frontend.controller.strategy.AddBookStrategy;
import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ImportDetailModel;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.model.InventoryModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.service.api.BookApiService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class ImportCreateController {

    @FXML private ComboBox<BookModel> cbBooks;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtImportPrice;

    @FXML private TableView<ImportDetailModel> tvCart;
    @FXML private TableColumn<ImportDetailModel, String> colTitle;
    @FXML private TableColumn<ImportDetailModel, Integer> colQty;
    @FXML private TableColumn<ImportDetailModel, Double> colPrice;
    @FXML private TableColumn<ImportDetailModel, Double> colTotal;
    @FXML private TableColumn<ImportDetailModel, Void> colAction;

    @FXML private Label lblTotalCartCost;
    @FXML private Button btnSubmit;

    private final ObservableList<ImportDetailModel> cartList = FXCollections.observableArrayList();
    private final ObservableList<BookModel> allBooks = FXCollections.observableArrayList();

    private ImportInteractor interactor;
    private BookApiService bookApiService;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(400));

    @FXML
    public void initialize() {
        this.interactor = new ImportInteractor(new ImportManagementModel());
        this.bookApiService = new BookApiService();

        setupTable();
        setupComboBox();

        loadDefaultBooks();

        cartList.addListener((ListChangeListener<ImportDetailModel>) c -> calculateTotalCost());
    }

    private void setupComboBox() {
        cbBooks.setItems(allBooks);
        cbBooks.setConverter(new StringConverter<>() {
            @Override
            public String toString(BookModel book) { return book == null ? "" : book.getTitle(); }

            @Override
            public BookModel fromString(String string) {
                return cbBooks.getItems().stream().filter(b -> b.getTitle().equals(string)).findFirst().orElse(null);
            }
        });

        cbBooks.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            final BookModel selected = cbBooks.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getTitle().equals(newValue)) return;

            searchDebounce.setOnFinished(event -> performSearch(newValue));
            searchDebounce.playFromStart();
        });
    }

    private void performSearch(String keyword) {
        if (keyword.trim().isEmpty()) {
            loadDefaultBooks();
            return;
        }

        bookApiService.searchBooks(keyword.trim()).thenAccept(dtoList -> {
            Platform.runLater(() -> {
                var bookModels = dtoList.stream().map(dto -> {
                    BookModel book = new BookModel();
                    book.setId(dto.getId());
                    book.setTitle(dto.getTitle());
                    return book;
                }).toList();

                allBooks.setAll(bookModels);
                if (!bookModels.isEmpty() && cbBooks.getEditor().isFocused()) {
                    cbBooks.show();
                }
            });
        }).exceptionally(ex -> {
            System.err.println("Lỗi khi tìm kiếm sách: " + ex.getMessage());
            return null;
        });
    }

    private void loadDefaultBooks() {
        bookApiService.fetchBooks(0, 50).thenAccept(pageData -> {
            Platform.runLater(() -> {
                var bookModels = pageData.getContent().stream().map(dto -> {
                    BookModel book = new BookModel();
                    book.setId(dto.getId());
                    book.setTitle(dto.getTitle());
                    return book;
                }).toList();

                allBooks.setAll(bookModels);
                cbBooks.setPromptText("Tìm kiếm tên sách...");
            });
        }).exceptionally(ex -> {
            System.err.println("Lỗi tải danh sách sách ban đầu: " + ex.getMessage());
            return null;
        });
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        // ĐÃ FIX: Chuyển $%.2f thành %,.0f đ cho Giá Nhập
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText((empty || price == null) ? null : String.format("%,.0f đ", price));
            }
        });

        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else {
                    setText(String.format("%,.0f đ", total));
                    setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-alignment: center-right;");
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("🗑");
            {
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-cursor: hand; -fx-font-size: 16px;");
                btnDelete.setOnAction(event -> cartList.remove(getIndex()));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });

        tvCart.setItems(cartList);
    }

    @FXML
    private void handleAddToList() {
        BookModel selectedBook = cbBooks.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một cuốn sách từ danh sách!").show();
            return;
        }

        try {
            int qty = Integer.parseInt(txtQuantity.getText());
            double price = Double.parseDouble(txtImportPrice.getText());

            if (qty <= 0 || price < 0) {
                new Alert(Alert.AlertType.WARNING, "Số lượng phải > 0 và Giá nhập không được âm!").show();
                return;
            }

            boolean exists = false;
            for (ImportDetailModel item : cartList) {
                if (item.getBookId() == selectedBook.getId()) {
                    item.setQuantity(item.getQuantity() + qty);
                    item.setImportPrice(price);
                    exists = true;
                    tvCart.refresh();
                    calculateTotalCost();
                    break;
                }
            }

            if (!exists) {
                ImportDetailModel newItem = new ImportDetailModel();
                newItem.setBookId(selectedBook.getId());
                newItem.setBookTitle(selectedBook.getTitle());
                newItem.setQuantity(qty);
                newItem.setImportPrice(price);
                cartList.add(newItem);
            }

            cbBooks.getSelectionModel().clearSelection();
            cbBooks.getEditor().clear();
            txtQuantity.setText("1");

            txtImportPrice.setText("0");

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập định dạng số hợp lệ!").show();
        }
    }

    private void calculateTotalCost() {
        double total = cartList.stream().mapToDouble(ImportDetailModel::getLineTotal).sum();
        lblTotalCartCost.setText(String.format("%,.0f đ", total));
    }

    @FXML
    private void handleSubmitImport() {
        if (cartList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Danh sách nhập hàng đang trống!").show();
            return;
        }

        btnSubmit.setDisable(true);

        interactor.createImport(cartList).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Tạo phiếu nhập thành công!").show();
                    cartList.clear();

                    btnSubmit.setDisable(false);
                    NavigationService.getInstance().navigateTo(PageType.IMPORT);
                } else {
                    btnSubmit.setDisable(false);
                    new Alert(Alert.AlertType.ERROR, "Lỗi khi lưu phiếu nhập lên Server!").show();
                }
            });
        });
    }

    @FXML
    private void handleAddNewBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/BookFormView.fxml"));
            Parent root = loader.load();

            BookFormController controller = loader.getController();

            InventoryInteractor tempInventoryInteractor = new InventoryInteractor(new InventoryModel());
            controller.setBook(new BookModel(), new AddBookStrategy(tempInventoryInteractor));

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isSaveClicked()) {
                BookModel newBook = controller.getCurrentBook();

                ImportDetailModel newItem = new ImportDetailModel();
                newItem.setBookId(newBook.getId());
                newItem.setBookTitle(newBook.getTitle());
                newItem.setQuantity(newBook.getQuantity() != null ? newBook.getQuantity() : 1);
                newItem.setImportPrice(newBook.getPrice() != null ? newBook.getPrice() : 0.0);

                cartList.add(newItem);
                allBooks.add(newBook);
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi mở form thêm sách: " + e.getMessage());
            e.printStackTrace();
        }
    }
}