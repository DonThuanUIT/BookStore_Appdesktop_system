package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ImportInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ImportDetailModel;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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

    // Giỏ hàng ảo chứa sách chờ nhập
    private final ObservableList<ImportDetailModel> cartList = FXCollections.observableArrayList();
    private ImportInteractor interactor;

    @FXML
    public void initialize() {
        // Khởi tạo Interactor để dùng API Create
        this.interactor = new ImportInteractor(new ImportManagementModel());

        setupTable();

        // Lắng nghe sự thay đổi của giỏ hàng để tự động tính lại Tổng tiền
        cartList.addListener((ListChangeListener<ImportDetailModel>) c -> calculateTotalCost());

        // TODO: Cần gọi API lấy danh sách sách đổ vào cbBooks (Sẽ xử lý sau)
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        // Format Tiền tệ
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

        // Nút Xóa dòng trong Giỏ hàng
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
        // Logic giả lập tạm thời (Fake Data) để test Giao diện
        // Lát nữa có API List Books ta sẽ lấy dữ liệu thật từ cbBooks.getValue()
        try {
            int qty = Integer.parseInt(txtQuantity.getText());
            double price = Double.parseDouble(txtImportPrice.getText());

            ImportDetailModel item = new ImportDetailModel();
            item.setBookId(1L); // Giả lập ID
            item.setBookTitle("Sách Test Import (Chưa có API)");
            item.setQuantity(qty);
            item.setImportPrice(price);

            cartList.add(item);

            // Reset ô nhập
            txtQuantity.setText("1");
            txtImportPrice.setText("0.00");

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Số lượng hoặc Giá nhập không hợp lệ!").show();
        }
    }

    private void calculateTotalCost() {
        double total = cartList.stream().mapToDouble(ImportDetailModel::getLineTotal).sum();
        lblTotalCartCost.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handleSubmitImport() {
        if (cartList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Giỏ hàng trống!").show();
            return;
        }

        interactor.createImport(cartList).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Tạo phiếu nhập thành công!").show();
                    cartList.clear();
                    // Điều hướng về màn hình Lịch sử
                    NavigationService.getInstance().navigateTo(PageType.IMPORT);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Lỗi khi tạo phiếu nhập!").show();
                }
            });
        });
    }

    @FXML
    private void handleAddNewBook() {
        System.out.println("Mở Form tạo sách mới...");
        // TODO: Mở Pop-up BookFormView.fxml đã làm ở Inventory
    }
}