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



public class HomeController extends BaseController {

    @FXML private Label lblWelcome;
    @FXML private FlowPane booksContainer;
    @FXML private BookDetailSidePanelController bookDetailSidePanelController;
    @FXML private TextField txtSearch;
    @FXML private MenuButton btnSearchType; // fx:id mới cho MenuButton

    private final HomeModel model;
    private final HomeInteractor interactor;

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
                                () -> AlertUtils.promptQuantityForCart(book.getTitle())
                                        .ifPresent(qty -> CartStore.getInstance().addBook(book, qty))
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

    @FXML
    public void handleTypeSelect(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        btnSearchType.setText(item.getText());
    }

    @FXML
    private void handleHomeSearch() {
        String query = txtSearch.getText();
        String type = btnSearchType.getText();

        System.out.println("Redirecting to Shop with filter: " + type + " = " + query);

//         NavigationService.getInstance().navigateTo(PageType.SHOP, new SearchFilter(query, type));
    }
}
