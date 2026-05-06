package com.bookstore.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class BookCardController {

    @FXML private VBox rootCard;
    @FXML private ImageView imgCover;
    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblPrice;
    @FXML private Button btnAdd;

    // Các hàm callback (để báo cho HomeController biết thẻ nào bị click)
    private Runnable onCardClicked;
    private Runnable onAddClicked;

    @FXML
    public void initialize() {
        // Bắt sự kiện người dùng click vào bất kỳ đâu trên thẻ sách (Để mở bảng Book Details)
        rootCard.setOnMouseClicked(event -> {
            if (onCardClicked != null) {
                onCardClicked.run();
            }
        });
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        // Ngăn không cho sự kiện click lan ra rootCard (Tránh mở bảng Details khi đang bấm nút Add)
        event.consume();
        if (onAddClicked != null) {
            onAddClicked.run();
        }
    }

    /**
     * Hàm dùng để nạp dữ liệu sách từ Database vào giao diện
     */
    public void setBookData(String title, String author, String price, String imagePath) {
        lblTitle.setText(title);
        lblAuthor.setText(author);
        lblPrice.setText(price);

        try {
            // Load ảnh từ thư mục. (Lưu ý: Bạn cần có ảnh thật để test)
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            imgCover.setImage(image);
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh: " + imagePath);
        }
    }

    /**
     * Hàm dùng để gắn sự kiện click từ HomeController truyền vào
     */
    public void setCallbacks(Runnable onCardClicked, Runnable onAddClicked) {
        this.onCardClicked = onCardClicked;
        this.onAddClicked = onAddClicked;
    }
}