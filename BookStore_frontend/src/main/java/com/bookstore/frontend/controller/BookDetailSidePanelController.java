package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.BookModel;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class BookDetailSidePanelController {
    @FXML private VBox rootPanel;
    @FXML private ImageView imgCover;
    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblPrice;


    @FXML
    public void initialize() {
        rootPanel.setVisible(false);
        rootPanel.setManaged(false);
    }

    public void setBookDetailDataAndShow(BookModel book) {
        lblTitle.setText(book.getTitle());
        lblAuthor.setText("By " + book.getAuthorName());
        lblPrice.setText(String.format("$%.2f", book.getPrice()));

        try {
            if (book.getImageUrl() != null && !book.getImageUrl().isBlank()) {
                Image image = new Image(book.getImageUrl(), true);
                imgCover.setImage(image);
            } else {
                imgCover.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("SidePanel - Lỗi nạp ảnh Cloudinary: " + book.getImageUrl());
        }

        // --- LOGIC ANIMATION MỞ PANEL (SLIDE IN + FADE IN) ---

        // 1. Phải bật hiển thị trước thì mới thấy được Animation
        rootPanel.setVisible(true);
        rootPanel.setManaged(true);

        // 2. Hiệu ứng trượt: Đẩy Panel ra xa 100px về bên phải, rồi kéo về vị trí gốc (0)
        TranslateTransition translate = new TranslateTransition(Duration.millis(350), rootPanel);
        translate.setFromX(100);
        translate.setToX(0);

        // 3. Hiệu ứng mờ: Từ hoàn toàn trong suốt (0) dần hiện rõ lên (1)
        FadeTransition fade = new FadeTransition(Duration.millis(350), rootPanel);
        fade.setFromValue(0);
        fade.setToValue(1);

        // 4. Gom 2 hiệu ứng chạy song song cùng lúc
        ParallelTransition slideIn = new ParallelTransition(translate, fade);
        slideIn.play();
    }


    @FXML
    public void handleClose() {
        // --- LOGIC ANIMATION ĐÓNG PANEL (SLIDE OUT + FADE OUT) ---

        // 1. Trượt ngược lại ra ngoài lề phải
        TranslateTransition translate = new TranslateTransition(Duration.millis(250), rootPanel);
        translate.setFromX(0);
        translate.setToX(100);

        // 2. Mờ dần đi
        FadeTransition fade = new FadeTransition(Duration.millis(250), rootPanel);
        fade.setFromValue(1);
        fade.setToValue(0);

        ParallelTransition slideOut = new ParallelTransition(translate, fade);

        // 3. QUAN TRỌNG: Đợi Animation chạy xong (250ms) thì mới tắt hiển thị để giải phóng layout
        slideOut.setOnFinished(event -> {
            rootPanel.setVisible(false);
            rootPanel.setManaged(false);
        });

        slideOut.play();
    }
}
