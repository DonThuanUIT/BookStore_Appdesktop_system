package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.HomeInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.HomeModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class HomeController extends BaseController {

    @FXML private Label lblWelcome;
    @FXML private FlowPane booksContainer;
    @FXML private BookDetailSidePanelController bookDetailSidePanelController;

    private final HomeModel model;
    private final HomeInteractor interactor;

    // TODO: Bạn hãy upload 1 tấm ảnh mặc định lên Cloudinary và thay link vào đây
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public HomeController() {
        this.model = new HomeModel();
        this.interactor = new HomeInteractor(this.model);
    }

    @FXML
    public void initialize() {
        lblWelcome.textProperty().bind(model.welcomeMessageProperty());
        loadBooks();
    }

    @Override
    public void onNavigate(Object data) {
        String username = (data instanceof String) ? (String) data : "Neth Reader";
        interactor.loadDashboardData(username);
    }

    private void loadBooks() {
        interactor.getLatestBooks().thenAccept(books -> {
            javafx.application.Platform.runLater(() -> {
                booksContainer.getChildren().clear();

                if (books.isEmpty()) {
                    System.out.println("No books returned from Backend!");
                    return;
                }

                for (BookModel book : books) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                        javafx.scene.Node cardNode = loader.load();

                        String formattedPrice = String.format("$%.2f", book.getPrice());

                        String imagePath = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                                ? book.getImageUrl()
                                : DEFAULT_COVER_URL;

                        BookCardController cardController = loader.getController();
                        cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, imagePath);

                        cardController.setCallbacks(
                                () -> bookDetailSidePanelController.setBookDetailDataAndShow(book),
                                () -> System.out.println("Đã thêm vào giỏ: " + book.getTitle())
                        );

                        booksContainer.getChildren().add(cardNode);
                    } catch (Exception e) {
                        System.err.println("Lỗi nạp UI thẻ sách cho cuốn: " + book.getTitle());
                    }
                }
            });
        });
    }


    @FXML
    public void handleViewAll() {
        NavigationService.getInstance().navigateTo(PageType.SHOP);
    }
}