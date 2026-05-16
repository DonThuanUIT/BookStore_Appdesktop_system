package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ShopController {

    @FXML private TextField txtSearch;
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane booksContainer;
    @FXML private Label lblStatus;
    @FXML private VBox categoriesContainer; // Đặt fx:id cho VBox chứa CheckBox trong FXML

    @FXML private BookDetailSidePanelController bookDetailSidePanelController;

    private ShopModel model;
    private ShopInteractor interactor;
    private List<BookModel> originalBooksList = new ArrayList<>();

    // Ảnh bìa mặc định khi sách chưa được cập nhật ảnh
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public ShopController() {
        this.model = new ShopModel();
        this.interactor = new ShopInteractor(this.model);
    }

    @FXML
    public void initialize() {
        setupUI();
        setupRealTimeFilters();
        loadInitialData();
    }

    private void setupUI() {
        cbSort.setItems(FXCollections.observableArrayList(
                "Newest",
                "Price: Low to High",
                "Price: High to Low"
        ));
        cbSort.setValue("Newest");
    }

    private void loadInitialData() {
        if (lblStatus != null) lblStatus.setText("Loading data...");

        interactor.getBooksPage(0, 50).thenAccept(pageDto -> {
            Platform.runLater(() -> {
                if (pageDto.getContent().isEmpty()) {
                    if (lblStatus != null) lblStatus.setText("There are no books in stock.");
                    return;
                }

                originalBooksList = pageDto.getContent();
                if (lblStatus != null) lblStatus.setText("");
                executeFilter();
            });
        });
    }

    private void setupRealTimeFilters() {
        txtSearch.textProperty().addListener((obs, old, newVal) -> executeFilter());

        // THÊM: Lắng nghe 2 ô nhập giá
        txtMinPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());
        txtMaxPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());

        // Thêm lắng nghe cho ComboBox sắp xếp luôn cho đồng bộ
        cbSort.valueProperty().addListener((obs, old, newVal) -> executeFilter());

        for (Node node : categoriesContainer.getChildren()) {
            if (node instanceof CheckBox cb) {
                cb.selectedProperty().addListener((obs, old, newVal) -> executeFilter());
            }
        }
    }

    private void executeFilter() {
        List<String> selectedCategories = categoriesContainer.getChildren().stream()
                .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                .map(node -> ((CheckBox) node).getText())
                .toList();

        List<BookModel> filteredBooks = interactor.applyClientSideFilters(
                originalBooksList,
                txtSearch.getText(),
                selectedCategories, // Truyền list thể loại vào
                parseDoubleSafe(txtMinPrice.getText()),
                parseDoubleSafe(txtMaxPrice.getText()),
                cbSort.getValue()
        );
        renderBooks(filteredBooks);
    }
    private void renderBooks(List<BookModel> books) {
        booksContainer.getChildren().clear();
        if (books.isEmpty()) return;

        for (BookModel book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                Node cardNode = loader.load();

                String formattedPrice = String.format("$%.2f", book.getPrice());
                String imageUrl = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                        ? book.getImageUrl()
                        : DEFAULT_COVER_URL;

                BookCardController cardController = loader.getController();
                cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, imageUrl);

                cardController.setCallbacks(
                        () -> {
                            if (bookDetailSidePanelController != null) {
                                bookDetailSidePanelController.setBookDetailDataAndShow(book);
                            } else {
                                System.err.println("Lỗi: Component bookSidePanel chưa được nạp (Null)!");
                            }
                        },
                        () -> AlertUtils.promptQuantityForCart(book.getTitle())
                                .ifPresent(qty -> CartStore.getInstance().addBook(book, qty))
                );

                booksContainer.getChildren().add(cardNode);
            } catch (Exception e) {
                System.err.println("Error rendering Shop book card:" + book.getTitle());
            }
        }
    }

    private Double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}