package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.interactor.ShopInteractor; // Lưu ý: package navigation có thể khác tùy cấu trúc của bạn
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class ShopController extends BaseController {

    @FXML
    private FlowPane bookContainer;

    private final ShopModel model;
    private final ShopInteractor interactor;

    public ShopController() {
        this.model = new ShopModel();
        this.interactor = new ShopInteractor(this.model);
    }

    @Override
    public void onNavigate(Object data) {
        // 1. Yêu cầu Interactor nạp dữ liệu (Sắp tới sẽ đổi thành gọi API thật ở đây)
        interactor.loadAllBooks();

        // 2. Xóa các card cũ trong Container
        bookContainer.getChildren().clear();

        // 3. Duyệt danh sách sách trong Model và vẽ giao diện cho từng cuốn
        for (BookModel book : model.getBooks()) { // Đã đổi thành BookModel
            bookContainer.getChildren().add(createBookCard(book));
        }
    }

    /**
     * Hàm tạo nhanh một giao diện Card sách.
     */
    private VBox createBookCard(BookModel book) { // Đã đổi thành BookModel
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card");
        card.setStyle("-fx-border-color: #ddd; -fx-padding: 15; -fx-alignment: center; -fx-pref-width: 200; -fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Giả lập ảnh
        VBox imgPlaceholder = new VBox();
        imgPlaceholder.setPrefSize(120, 160);
        imgPlaceholder.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: -fx-accent-gold;");

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-weight: bold;");

        // Sử dụng getAuthorName() từ BookModel
        String authorStr = book.getAuthorName() != null ? book.getAuthorName() : "Đang cập nhật";
        Label author = new Label(authorStr);
        author.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

        // Xử lý hiển thị giá tiền từ kiểu Double sang chuỗi có dấu phẩy (VD: 120,000đ)
        String priceStr = "0đ";
        if (book.getPrice() != null) {
            priceStr = String.format("%,.0fđ", book.getPrice());
        }
        Label price = new Label(priceStr);
        price.setStyle("-fx-text-fill: -fx-primary-black; -fx-font-weight: bold;");

        Button btnAdd = new Button("Add to Cart");
        // Giữ nguyên các class của bạn
        btnAdd.getStyleClass().add("btn-primary");

        card.getChildren().addAll(imgPlaceholder, title, author, price, btnAdd);
        return card;
    }
}