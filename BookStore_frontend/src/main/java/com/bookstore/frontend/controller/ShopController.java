package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookDTO;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.navigation.ShopInteractor;
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
        // 1. Yêu cầu Interactor nạp dữ liệu giả
        interactor.loadAllBooks();

        // 2. Xóa các card cũ trong Container
        bookContainer.getChildren().clear();

        // 3. Duyệt danh sách sách trong Model và vẽ giao diện cho từng cuốn
        for (BookDTO book : model.getBooks()) {
            bookContainer.getChildren().add(createBookCard(book));
        }
    }

    /**
     * Hàm tạo nhanh một giao diện Card sách (Mock UI).
     * Sau này chúng ta sẽ tách Card này ra một file FXML riêng cho chuyên nghiệp.
     */
    private VBox createBookCard(BookDTO book) {
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card"); // Bạn có thể thêm style này vào theme.css
        card.setStyle("-fx-border-color: #ddd; -fx-padding: 15; -fx-alignment: center; -fx-pref-width: 200; -fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Giả lập ảnh (Vì chưa có ảnh thật, ta dùng một khối màu)
        VBox imgPlaceholder = new VBox();
        imgPlaceholder.setPrefSize(120, 160);
        imgPlaceholder.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: -fx-accent-gold;");

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-weight: bold;");

        Label author = new Label(book.getAuthor());
        author.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

        Label price = new Label(book.getPrice());
        price.setStyle("-fx-text-fill: -fx-primary-black; -fx-font-weight: bold;");

        Button btnAdd = new Button("Add to Cart");
        btnAdd.getStyleClass().add("btn-primary");

        card.getChildren().addAll(imgPlaceholder, title, author, price, btnAdd);
        return card;
    }
}