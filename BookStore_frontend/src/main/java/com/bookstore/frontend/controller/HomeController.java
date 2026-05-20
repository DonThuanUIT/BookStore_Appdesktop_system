package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.HomeInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.HomeModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController extends BaseController {

    @FXML private Label lblWelcome;
    @FXML private FlowPane booksContainer;
    @FXML private BookDetailSidePanelController bookDetailSidePanelController;
    @FXML private TextField txtSearch;
    @FXML private MenuButton btnSearchType;

    private final HomeModel model;
    private final HomeInteractor interactor;

    private List<BookModel> originalBooksList = new ArrayList<>();
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public HomeController() {
        this.model = new HomeModel();
        this.interactor = new HomeInteractor(this.model);
    }

    @FXML
    public void initialize() {
        if (lblWelcome != null) lblWelcome.textProperty().bind(model.welcomeMessageProperty());
        loadBooks();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            executeHomeFilterWithType(btnSearchType.getText());
        });
    }

    @Override
    public void onNavigate(Object data) {
        String username = (data instanceof String) ? (String) data : "Neth Reader";
        interactor.loadDashboardData(username);
    }

    private void loadBooks() {
        interactor.getLatestBooks().thenAccept(books -> {
            this.originalBooksList = books;
            javafx.application.Platform.runLater(() -> {
                renderBooks(this.originalBooksList);
            });
        });
    }

    private void executeHomeFilter() {
        executeHomeFilterWithType(btnSearchType.getText());
    }

    private void executeHomeFilterWithType(String searchType) {
        String rawQuery = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";

        if (rawQuery.isEmpty()) {
            renderBooks(originalBooksList);
            return;
        }

        // CẢI TIẾN: Chuẩn hóa Unicode loại bỏ hoàn toàn sự lệch pha tổ hợp dấu tiếng Việt
        String query = Normalizer.normalize(rawQuery, Normalizer.Form.NFC);
        String activeType = searchType != null ? searchType.trim() : "Title";

        List<BookModel> filteredBooks = originalBooksList.stream()
                .filter(book -> {
                    if (activeType.equalsIgnoreCase("Author")) {
                        if (book.getAuthorName() == null) return false;
                        String author = Normalizer.normalize(book.getAuthorName().toLowerCase(), Normalizer.Form.NFC);
                        return author.contains(query);
                    }
                    else if (activeType.equalsIgnoreCase("Category")) {
                        if (book.getCategoryNames() == null) return false;
                        return book.getCategoryNames().stream().anyMatch(catName -> {
                            String category = Normalizer.normalize(catName.toLowerCase(), Normalizer.Form.NFC);
                            return category.contains(query);
                        });
                    }
                    else {
                        if (book.getTitle() == null) return false;
                        String title = Normalizer.normalize(book.getTitle().toLowerCase(), Normalizer.Form.NFC);
                        return title.contains(query);
                    }
                })
                .collect(Collectors.toList());

        renderBooks(filteredBooks);
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

                String formattedPrice = String.format("%,.0f đ", book.getPrice());
                String imagePath = (book.getImageUrl() != null && !book.getImageUrl().isBlank())
                        ? book.getImageUrl()
                        : DEFAULT_COVER_URL;

                BookCardController cardController = loader.getController();
                cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, imagePath);

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
    public void handleTypeSelect(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String selectedType = item.getText();
        btnSearchType.setText(selectedType);
        executeHomeFilterWithType(selectedType);
    }

    @FXML
    private void handleHomeSearch() {
        executeHomeFilter();
    }
}