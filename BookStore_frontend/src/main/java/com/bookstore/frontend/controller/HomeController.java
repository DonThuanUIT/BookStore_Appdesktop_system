package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.HomeInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.HomeModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class HomeController extends BaseController {

    @FXML private Label lblWelcome;
    @FXML private FlowPane booksContainer; // Nơi chứa các thẻ sách

    // --- Các biến cho cột Panel Chi Tiết Sách ---
    @FXML private VBox sidePanel;
    @FXML private ImageView detailCover;
    @FXML private Label detailTitle;
    @FXML private Label detailAuthor;
    @FXML private Label detailPrice;

    private final HomeModel model;
    private final HomeInteractor interactor;

    public HomeController() {
        this.model = new HomeModel();
        this.interactor = new HomeInteractor(this.model);
    }

    @FXML
    public void initialize() {
        lblWelcome.textProperty().bind(model.welcomeMessageProperty());

        // Tạm thời gọi hàm tạo dữ liệu giả lập (Mock Data)
        loadBooks();
    }

    @Override
    public void onNavigate(Object data) {
        String username = (data instanceof String) ? (String) data : "Neth Reader";
        interactor.loadDashboardData(username);
    }

    /**
     * Hàm giả lập tải dữ liệu sách. Sau này bạn sẽ thay bằng việc lấy Data từ Database/API.
     */
    private void loadBooks() {
        interactor.getLatestBooks().thenAccept(books -> {
            // Update UI phải luôn chạy trên JavaFX Application Thread
            javafx.application.Platform.runLater(() -> {
                booksContainer.getChildren().clear(); // Xóa sạch rác cũ nếu có

                if (books.isEmpty()) {
                    System.out.println("Không có sách nào được trả về từ Backend!");
                    return;
                }

                for (BookModel book : books) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                        javafx.scene.Node cardNode = loader.load();

                        // Định dạng tiền tệ
                        String formattedPrice = String.format("$%.2f", book.getPrice());
                        // Thay ảnh null bằng ảnh mặc định nếu backend chưa có ảnh
                        String imagePath = (book.getImageUrl() != null) ? book.getImageUrl() : "/image/default_book.png";

                        BookCardController cardController = loader.getController();
                        cardController.setBookData(book.getTitle(), book.getAuthorName(), formattedPrice, imagePath);

                        // Gắn sự kiện click
                        cardController.setCallbacks(
                                () -> openSidePanel(book.getTitle(), book.getAuthorName(), formattedPrice, imagePath),
                                () -> System.out.println("Đã thêm vào giỏ: " + book.getTitle())
                        );

                        booksContainer.getChildren().add(cardNode);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Lỗi nạp UI thẻ sách cho cuốn: " + book.getTitle());
                    }
                }
            });
        });
    }

    /**
     * Mở Side Panel và đổ dữ liệu vào
     */
    private void openSidePanel(String title, String author, String price, String imgPath) {
        detailTitle.setText(title);
        detailAuthor.setText("By " + author);
        detailPrice.setText(price);

        try {
            Image image = new Image(getClass().getResourceAsStream(imgPath));
            detailCover.setImage(image);
        } catch (Exception e) {
            System.err.println("SidePanel - Không tìm thấy ảnh: " + imgPath);
        }

        // Hiện Panel
        sidePanel.setVisible(true);
        sidePanel.setManaged(true);
    }

    /**
     * Đóng Side Panel
     */
    @FXML
    public void closeSidePanel() {
        sidePanel.setVisible(false);
        sidePanel.setManaged(false);
    }

    @FXML
    public void handleViewAll() {
        NavigationService.getInstance().navigateTo(PageType.SHOP);
    }
}