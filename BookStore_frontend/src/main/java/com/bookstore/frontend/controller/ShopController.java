package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;

public class ShopController {

    // --- CÁC THÀNH PHẦN GIAO DIỆN (UI COMPONENTS) ---
    @FXML private TextField txtSearch;
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane booksContainer;
    @FXML private Label lblStatus; // Dùng để hiển thị trạng thái "Đang tải..." hoặc "Lỗi"

    // --- KIẾN TRÚC & DỮ LIỆU ---
    private ShopModel model;
    private ShopInteractor interactor;

    // Nơi lưu trữ "kho sách gốc" kéo từ Backend về để đem ra lọc
    private List<BookModel> originalBooksList = new ArrayList<>();

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

    /**
     * Khởi tạo các giá trị mặc định cho UI
     */
    private void setupUI() {
        cbSort.setItems(FXCollections.observableArrayList(
                "Newest",
                "Price: Low to High",
                "Price: High to Low"
        ));
        cbSort.setValue("Newest");
    }

    /**
     * Gắn "Tai nghe" (Listeners) để lọc sách ngay lập tức khi user thao tác
     */
    private void setupRealTimeFilters() {
        // Lắng nghe ô tìm kiếm (gõ chữ nào lọc chữ đó)
        txtSearch.textProperty().addListener((obs, oldText, newText) -> executeFilter());

        // Lắng nghe ô nhập Giá
        txtMinPrice.textProperty().addListener((obs, oldText, newText) -> executeFilter());
        txtMaxPrice.textProperty().addListener((obs, oldText, newText) -> executeFilter());

        // Lắng nghe ComboBox Sắp xếp
        cbSort.valueProperty().addListener((obs, oldVal, newVal) -> executeFilter());
    }

    /**
     * Lấy 50 cuốn sách đầu tiên từ Backend về khi vừa mở trang
     */
    private void loadInitialData() {
        if (lblStatus != null) lblStatus.setText("Đang tải dữ liệu...");

        // Tạm thời lấy trang 0, kích thước 50 cuốn để lọc ở Client
        interactor.getBooksPage(0, 50).thenAccept(pageDto -> {
            Platform.runLater(() -> {
                if (pageDto.getContent().isEmpty()) {
                    if (lblStatus != null) lblStatus.setText("Không có cuốn sách nào trong kho.");
                    return;
                }

                // Lưu lại sách gốc và gọi hàm lọc để hiển thị
                originalBooksList = pageDto.getContent();
                if (lblStatus != null) lblStatus.setText(""); // Xóa dòng "Đang tải..."

                executeFilter();
            });
        });
    }

    /**
     * Trích xuất thông tin từ UI và nhờ Interactor lọc dữ liệu
     */
    private void executeFilter() {
        String keyword = txtSearch.getText();
        String sortType = cbSort.getValue();

        Double minPrice = parseDoubleSafe(txtMinPrice.getText());
        Double maxPrice = parseDoubleSafe(txtMaxPrice.getText());

        // Gọi Interactor lọc sách
        List<BookModel> filteredBooks = interactor.applyClientSideFilters(
                originalBooksList,
                keyword,
                null, // Category tạm thời để null
                minPrice,
                maxPrice,
                sortType
        );

        // Vẽ lại danh sách sách đã lọc
        renderBooks(filteredBooks);
    }

    /**
     * Hàm vẽ UI thẻ sách (Tái sử dụng Component BookCard giống hệt trang Home)
     */
    private void renderBooks(List<BookModel> books) {
        booksContainer.getChildren().clear();

        if (books.isEmpty()) {
            // Có thể thêm 1 Label báo "Không tìm thấy sách phù hợp" ở đây
            return;
        }

        for (BookModel book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                Node cardNode = loader.load();

                String formattedPrice = String.format("$%.2f", book.getPrice());
                String imagePath = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                        ? book.getImageUrl()
                        : "/image/default_book.png";

                BookCardController cardController = loader.getController();
                cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, imagePath);

                // Gắn sự kiện click (Tùy chọn: Để sau này làm giỏ hàng)
                cardController.setCallbacks(
                        () -> System.out.println("Đang mở chi tiết sách: " + book.getTitle()),
                        () -> System.out.println("Đã thêm vào giỏ: " + book.getTitle())
                );

                booksContainer.getChildren().add(cardNode);
            } catch (Exception e) {
                System.err.println("Lỗi render thẻ sách Shop: " + book.getTitle());
                e.printStackTrace();
            }
        }
    }

    /**
     * Hàm phụ trợ chuyển chuỗi thành số, chống crash khi user gõ bậy (như "abc" vào ô giá)
     */
    private Double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null; // Bỏ qua nếu nhập sai format
        }
    }
}