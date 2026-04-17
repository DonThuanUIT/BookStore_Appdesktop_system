package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.model.dto.PageResponseDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopController extends BaseController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane bookContainer;
    @FXML private ComboBox<String> comboSearchType;
    @FXML private TextField txtSearch;
    @FXML private Label lblBookCount;

    private final ShopModel model;
    private final ShopInteractor interactor;

    private int currentPage = 0;
    private final int pageSize = 12;
    private boolean isLoading = false;
    private boolean isLastPage = false; // CHỐT CHẶN BẢO VỆ SERVER

    public ShopController() {
        this.model = new ShopModel();
        this.interactor = new ShopInteractor(this.model);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboSearchType.setItems(FXCollections.observableArrayList("Title", "Author", "Category"));
        comboSearchType.getSelectionModel().selectFirst();

        // Bắt sự kiện cuộn chuột
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            // Nếu cuộn gần đáy (98%) + Không tải dở + CHƯA PHẢI TRANG CUỐI
            if (newVal.doubleValue() >= 0.98 && !isLoading && !isLastPage) {
                loadMoreBooks();
            }
        });
    }

    @Override
    public void onNavigate(Object data) {
        resetShop();
        loadMoreBooks();
    }

    private void resetShop() {
        currentPage = 0;
        isLastPage = false;
        bookContainer.getChildren().clear();
        isLoading = false;
    }

    private void loadMoreBooks() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        // Gọi API ngầm (Không làm đơ UI)
        interactor.getBooksPage(currentPage, pageSize)
                .thenAccept(pageDto -> Platform.runLater(() -> {
                    List<BookModel> newBooks = pageDto.getContent();

                    if (newBooks != null && !newBooks.isEmpty()) {
                        // Lưu vào Model
                        if (currentPage == 0) model.setBooks(newBooks);
                        else model.addBooks(newBooks);

                        // Vẽ Thẻ sách
                        for (BookModel book : newBooks) {
                            bookContainer.getChildren().add(createBookCard(book));
                        }
                        currentPage++;
                        updateBookCount();
                    }

                    // CẬP NHẬT CỜ: Đã hết sách chưa?
                    isLastPage = pageDto.isLast();
                    isLoading = false;
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        isLoading = false;
                        System.err.println("Lỗi khi tải sách: " + ex.getMessage());
                        // Có thể gọi AlertUtils ở đây để báo lỗi lên màn hình
                    });
                    return null;
                });
    }

    private VBox createBookCard(BookModel book) {
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card-item");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(book.getImageUrl(), true)); // Load ảnh nền
        }
        imageView.setFitWidth(160);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);

        Label title = new Label(book.getTitle());
        title.getStyleClass().add("book-title-label");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);

        Label price = new Label(String.format("%,.0fđ", book.getPrice()));
        price.setStyle("-fx-text-fill: -fx-accent-gold; -fx-font-weight: bold;");

        card.getChildren().addAll(imageView, title, price);
        card.setOnMouseClicked(event -> showBookDetail(book));

        return card;
    }

    @FXML
    private void onSearch() {
        resetShop();
        loadMoreBooks();
    }

    private void updateBookCount() {
        lblBookCount.setText(bookContainer.getChildren().size() + " books available");
    }

    private void showBookDetail(BookModel book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/BookDetailView.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Book Detail - " + book.getTitle());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(scrollPane.getScene().getWindow());
            dialogStage.setScene(scene);

            BookDetailController controller = loader.getController();
            controller.setBookData(book);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}