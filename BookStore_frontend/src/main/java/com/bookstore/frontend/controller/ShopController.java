package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
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
        // Cập nhật các tùy chọn sắp xếp cho giống giao diện mới
        comboSearchType.setItems(FXCollections.observableArrayList("Popularity", "Price: Low to High", "Newest"));
        comboSearchType.getSelectionModel().selectFirst();

        // Vẫn GIỮ LẠI tính năng Infinite Scroll xịn xò của bạn
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 0.98 && !isLoading && !isLastPage) {
                loadMoreBooks();
            }
        });
    }

    @Override
    public void onNavigate(Object data) {
        resetShop();
        loadMoreBooks(); // Khi vừa vào trang sẽ tự động tải trang đầu tiên
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
                        if (currentPage == 0) model.setBooks(newBooks);
                        else model.addBooks(newBooks);

                        // Vòng lặp: Nhồi Component BookCard vào Grid
                        for (BookModel book : newBooks) {
                            Node cardNode = createBookCard(book);
                            if (cardNode != null) {
                                bookContainer.getChildren().add(cardNode);
                            }
                        }
                        currentPage++;
                        updateBookCount();
                    }

                    isLastPage = pageDto.isLast();
                    isLoading = false;
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        isLoading = false;
                        System.err.println("Lỗi khi tải sách từ server: " + ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * TRÁI TIM CỦA REFACTORING: Tái sử dụng Component thay vì hardcode VBox
     */
    private Node createBookCard(BookModel book) {
        try {
            // LƯU Ý: Đảm bảo đường dẫn này khớp với nơi bạn lưu file BookCard.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
            Node cardNode = loader.load();

            BookCardController cardController = loader.getController();

            // Format giá tiền cho đẹp
            String formattedPrice = String.format("$%.2f", book.getPrice());

            // Nạp dữ liệu vào thẻ sách
            cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, book.getImageUrl());

            // Gắn sự kiện (Callback)
            cardController.setCallbacks(
                    () -> showBookDetail(book), // Click vào thẻ -> Mở bảng chi tiết
                    () -> handleAddToCart(book) // Click vào nút Add to Cart
            );

            return cardNode;
        } catch (Exception e) {
            System.err.println("Lỗi nạp UI thẻ sách cho: " + book.getTitle());
            e.printStackTrace();
            return null;
        }
    }

    private void updateBookCount() {
        int currentCount = bookContainer.getChildren().size();
        lblBookCount.setText("Showing " + currentCount + " results");
    }

    private void handleAddToCart(BookModel book) {
        // TODO: Chuyển logic này sang Interactor để lưu vào Giỏ hàng thực tế
        System.out.println(">>> ĐÃ THÊM VÀO GIỎ HÀNG: " + book.getTitle() + " | Giá: " + book.getPrice());
    }

    private void showBookDetail(BookModel book) {
        // Giữ nguyên logic cũ của bạn (Mở popup cửa sổ mới)
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