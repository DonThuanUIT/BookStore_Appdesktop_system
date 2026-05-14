package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ImportDetailModel;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.service.api.BookApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    // Nút Hoàn tất
    @FXML private Button btnSubmit;

    private final ObservableList<ImportDetailModel> cartList = FXCollections.observableArrayList();
    private final ObservableList<BookModel> allBooks = FXCollections.observableArrayList();

    private ImportInteractor interactor;
    private BookApiService bookApiService;

    @FXML
    public void initialize() {
        this.interactor = new ImportInteractor(new ImportManagementModel());
        this.bookApiService = new BookApiService();

        setupTable();
        setupComboBox();
        loadBooksFromApi();

        cartList.addListener((ListChangeListener<ImportDetailModel>) c -> calculateTotalCost());
    }

    // --- 1. CẤU HÌNH COMBOBOX & AUTOCOMPLETE (ĐÃ FIX LỖI CRASH JAVAFX) ---
    private void setupComboBox() {
        cbBooks.setConverter(new StringConverter<BookModel>() {
            @Override
            public String toString(BookModel book) {
                return book == null ? "" : book.getTitle();
            }

            @Override
            public BookModel fromString(String string) {
                return cbBooks.getItems().stream()
                        .filter(b -> b.getTitle().equals(string))
                        .findFirst().orElse(null);
            }
        });

        FilteredList<BookModel> filteredBooks = new FilteredList<>(allBooks, p -> true);
        cbBooks.setItems(filteredBooks);

        cbBooks.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final TextField editor = cbBooks.getEditor();
            final BookModel selected = cbBooks.getSelectionModel().getSelectedItem();

            if (selected != null && selected.getTitle().equals(editor.getText())) {
                return;
            }

            // Fix bug JavaFX: Bỏ qua nếu giá trị null
            if (newValue == null) return;

            filteredBooks.setPredicate(book -> {
                if (newValue.isEmpty()) return true;
                return book.getTitle().toLowerCase().contains(newValue.toLowerCase());
            });

            // Bọc việc xổ UI vào luồng chạy sau để tránh lỗi "The start must be <= the end"
            Platform.runLater(() -> {
                if (editor.isFocused() && !filteredBooks.isEmpty()) {
                    cbBooks.show();
                } else {
                    cbBooks.hide();
                }
            });
        });
    }

    // --- 2. GỌI API LẤY DANH SÁCH SÁCH ---
    private void loadBooksFromApi() {
        bookApiService.fetchBooks(0, 1000).thenAccept(pageData -> {
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
            System.err.println("Lỗi tải danh sách sách: " + ex.getMessage());
            return null;
        });
    }

    // --- 3. CẤU HÌNH BẢNG GIỎ HÀNG ---
    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText((empty || price == null) ? null : String.format("$%.2f", price));
            }
        });
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else {
                    setText(String.format("$%.2f", total));
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

    // --- 4. XỬ LÝ NÚT: THÊM VÀO DANH SÁCH ---
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
            txtImportPrice.setText("0.00");

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập định dạng số hợp lệ!").show();
        }
    }

    private void calculateTotalCost() {
        double total = cartList.stream().mapToDouble(ImportDetailModel::getLineTotal).sum();
        lblTotalCartCost.setText(String.format("$%.2f", total));
    }

    // --- 5. SUBMIT PHIẾU NHẬP LÊN BACKEND ---
    @FXML
    private void handleSubmitImport() {
        if (cartList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Danh sách nhập hàng đang trống!").show();
            return;
        }

        // CHỐNG DOUBLE-CLICK: Khóa nút ngay khi bắt đầu gửi request
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

    // --- 6. XỬ LÝ MỞ FORM THÊM SÁCH ---
    @FXML
    private void handleAddNewBook() {
        try {
            // Mở Pop-up BookFormView với đường dẫn đã sửa chuẩn xác
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/BookFormView.fxml"));
            Parent root = loader.load();

            BookFormController controller = loader.getController();
            controller.setBook(new BookModel(), false); // Tạo mới sách

            Stage stage = new Stage();
            stage.setTitle("Thêm Sách Mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            stage.showAndWait();

            // Nếu user bấm "Save", tải lại danh sách sách
            if (controller.isSaveClicked()) {
                System.out.println("Đã thêm sách mới, đang tải lại danh sách dropdown...");
                loadBooksFromApi();
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi mở form thêm sách: " + e.getMessage());
            e.printStackTrace();
        }
    }
}