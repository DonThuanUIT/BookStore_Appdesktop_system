package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ShopController {

    @FXML private TextField txtSearch;
    @FXML private MenuButton btnSearchType; // Nút chọn kiểu tìm kiếm (Title/Author)
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane booksContainer;
    @FXML private Label lblStatus;
    @FXML private VBox categoriesContainer;

    @FXML private BookDetailSidePanelController bookDetailSidePanelController;

    private ShopModel model;
    private ShopInteractor interactor;
    private List<BookModel> originalBooksList = new ArrayList<>();

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
        if (cbSort != null) {
            cbSort.setItems(FXCollections.observableArrayList(
                    "Newest",
                    "Price: Low to High",
                    "Price: High to Low"
            ));
            cbSort.setValue("Newest");
        }

        // Đặt nhãn ban đầu hiển thị mặc định
        if (btnSearchType != null) {
            btnSearchType.setText("Title");
        }
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
        txtMinPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());
        txtMaxPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());
        cbSort.valueProperty().addListener((obs, old, newVal) -> executeFilter());

        if (categoriesContainer != null) {
            for (Node node : categoriesContainer.getChildren()) {
                if (node instanceof CheckBox cb) {
                    cb.selectedProperty().addListener((obs, old, newVal) -> executeFilter());
                }
            }
        }
    }

    /**
     * Bắt sự kiện khi chuyển đổi kiểu tìm kiếm (Title / Author)
     */
    @FXML
    public void handleTypeSelect(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String selectedType = item.getText();
        btnSearchType.setText(selectedType);

        // Chạy lại bộ lọc thời gian thực ngay khi người dùng bấm đổi kiểu
        executeFilter();
    }

    private void executeFilter() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim() : "";
        String searchType = btnSearchType != null ? btnSearchType.getText().trim() : "Title";

        // Khởi tạo dữ liệu gán 1 lần duy nhất (effectively final)
        final List<String> selectedCategories = (categoriesContainer == null) ? new ArrayList<>()
                : categoriesContainer.getChildren().stream()
                .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                .map(node -> ((CheckBox) node).getText())
                .toList();

        if (lblStatus != null) lblStatus.setText("Searching...");

        // TRƯỜNG HỢP 1: Ô tìm kiếm trống -> Chỉ lọc theo khoảng giá, checkbox bên dưới và sắp xếp
        if (keyword.isEmpty()) {
            List<BookModel> finalFilteredBooks = interactor.applyClientSideFilters(
                    originalBooksList, "", selectedCategories,
                    parseDoubleSafe(txtMinPrice.getText()), parseDoubleSafe(txtMaxPrice.getText()), cbSort.getValue()
            );
            renderBooks(finalFilteredBooks);
            if (lblStatus != null) lblStatus.setText("Found " + finalFilteredBooks.size() + " books.");
            return;
        }

        // TRƯỜNG HỢP 2: Tìm kiếm theo Title hoặc Author -> Gọi xuống API Backend truy vết sâu dưới DB
        interactor.searchBooksFromBackend(keyword).thenAccept(booksFromBackend -> {
            Platform.runLater(() -> {
                // Sàng lọc lớp hai đảm bảo kết quả trùng khít với Type (Title hay Author) đang chọn trên MenuButton
                List<BookModel> typeSafetyBooks = booksFromBackend.stream()
                        .filter(book -> {
                            if (searchType.equalsIgnoreCase("Author")) {
                                return book.getAuthorName() != null && book.getAuthorName().toLowerCase().contains(keyword.toLowerCase());
                            } else { // Mặc định là Title
                                return book.getTitle() != null && book.getTitle().toLowerCase().contains(keyword.toLowerCase());
                            }
                        }).toList();

                // Chạy tiếp qua bộ lọc giá, thể loại (Checkbox) và sắp xếp
                List<BookModel> finalFilteredBooks = interactor.applyClientSideFilters(
                        typeSafetyBooks, "", selectedCategories,
                        parseDoubleSafe(txtMinPrice.getText()), parseDoubleSafe(txtMaxPrice.getText()), cbSort.getValue()
                );

                renderBooks(finalFilteredBooks);

                if (lblStatus != null) {
                    if (finalFilteredBooks.isEmpty()) {
                        lblStatus.setText("No books found matching your criteria.");
                    } else {
                        lblStatus.setText("Found " + finalFilteredBooks.size() + " books.");
                    }
                }
            });
        });
    }

    private void renderBooks(List<BookModel> books) {
        booksContainer.getChildren().clear();
        if (books.isEmpty()) return;

        for (BookModel book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                Node cardNode = loader.load();

                String formattedPrice = String.format("%,.0f đ", book.getPrice());
                String imageUrl = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                        ? book.getImageUrl()
                        : DEFAULT_COVER_URL;

                BookCardController cardController = loader.getController();
                cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, imageUrl);

                cardController.setCallbacks(
                        () -> {
                            if (bookDetailSidePanelController != null) {
                                bookDetailSidePanelController.setBookDetailDataAndShow(book);
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