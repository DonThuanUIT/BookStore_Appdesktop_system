package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.HomeInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.HomeModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.service.QuoteService;
import com.bookstore.frontend.service.api.BookApiService;
import com.bookstore.frontend.util.BookMapper;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController extends BaseController {

    @FXML private Label lblWelcome;
    @FXML private Label lblQuote;
    @FXML private FlowPane booksContainer;
    @FXML private BookDetailSidePanelController bookDetailSidePanelController;
    @FXML private TextField txtSearch;

    private final HomeModel model;
    private final HomeInteractor interactor;
    private final BookApiService bookApiService;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(400));

    private List<BookModel> originalBooksList = new ArrayList<>();
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public HomeController() {
        this.model = new HomeModel();
        this.interactor = new HomeInteractor(this.model);
        this.bookApiService = new BookApiService();
    }

    @FXML
    public void initialize() {
        if (lblWelcome != null) lblWelcome.textProperty().bind(model.welcomeMessageProperty());

        if (lblQuote != null) {
            lblQuote.setText(QuoteService.getInstance().getRandomQuote());
        }

        loadBooks();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDebounce.setOnFinished(event -> executeHomeSearch());
            searchDebounce.playFromStart();
        });
    }

    @Override
    public void onNavigate(Object data) {
        String username = (data instanceof String) ? (String) data : "Rít Đờ";
        interactor.loadDashboardData(username);
    }

    private void loadBooks() {
        interactor.getLatestBooks().thenAccept(books -> {
            this.originalBooksList = books;
            Platform.runLater(() -> {
                renderBooks(this.originalBooksList);
            });
        });
    }

    private void executeHomeSearch() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim() : "";

        if (keyword.isEmpty()) {
            renderBooks(originalBooksList);
            return;
        }

        bookApiService.searchBooks(keyword).thenAccept(dtoList -> {
            List<BookModel> backendBooks = dtoList.stream()
                    .map(BookMapper::toModel)
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                renderBooks(backendBooks);
            });
        }).exceptionally(ex -> {
            System.err.println("Lỗi khi thực hiện tìm kiếm tổng lực tại Trang chủ: " + ex.getMessage());
            return null;
        });
    }

    private void renderBooks(List<BookModel> books) {
        booksContainer.getChildren().clear();

        if (books.isEmpty()) {
            System.out.println("No books match criteria or list is empty!");
            return;
        }

        for (BookModel book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                javafx.scene.Node cardNode = loader.load();

                // FIX: Sử dụng CurrencyUtils để hiển thị VND thống nhất
                String formattedPrice = com.bookstore.frontend.util.CurrencyUtils.formatVND(book.getPrice());
                String imagePath = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                        ? book.getImageUrl()
                        : DEFAULT_COVER_URL;

                BookCardController cardController = loader.getController();

                cardController.setBookData(book.getTitle(), book.getFormattedAuthors(), formattedPrice, imagePath);

                cardController.setCallbacks(
                        () -> bookDetailSidePanelController.setBookDetailDataAndShow(book),
                        () -> AlertUtils.promptQuantityForCart(book.getTitle())
                                .ifPresent(qty -> CartStore.getInstance().addBook(book, qty))
                );

                booksContainer.getChildren().add(cardNode);
            } catch (Exception e) {
                System.err.println("Lỗi nạp UI thẻ sách cho cuốn: " + book.getTitle());
            }
        }
    }

    @FXML
    public void handleViewAll() {
        NavigationService.getInstance().navigateTo(PageType.SHOP);
    }

    @FXML
    private void handleHomeSearch() {
        searchDebounce.stop();
        executeHomeSearch();
    }
}