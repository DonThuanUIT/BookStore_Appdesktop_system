package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.HomeInteractor;
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
        loadMockBooks();
    }

    @Override
    public void onNavigate(Object data) {
        String username = (data instanceof String) ? (String) data : "Neth Reader";
        interactor.loadDashboardData(username);
    }

    /**
     * Hàm giả lập tải dữ liệu sách. Sau này bạn sẽ thay bằng việc lấy Data từ Database/API.
     */
    private void loadMockBooks() {
        // Mảng dữ liệu test (Bạn có thể đổi tên file ảnh cho khớp với resource của bạn)
        String[][] mockBooks = {
                {"The Architecture of Shadows", "Eleanor Vance", "$24.99", "/image/book_shadows.png"},
                {"Whispers in the Glass", "Jonathan Reed", "$18.50", "/image/book_glass.png"},
                {"Echoes of the Forgotten", "Sarah Lin", "$32.00", "/image/book_echoes.png"},
                {"The Clockwork Heart", "David Alastair", "$21.95", "/image/book_clockwork.png"},
                {"The Great Gatsby", "F. Scott Fitzgerald", "$15.00", "/image/great_gatsby.png"}
        };

        try {
            for (String[] book : mockBooks) {
                // 1. Tải giao diện của một cuốn sách (Component)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                Node cardNode = loader.load();

                // 2. Lấy Controller của thẻ sách đó để truyền dữ liệu
                BookCardController cardController = loader.getController();
                cardController.setBookData(book[0], book[1], book[2], book[3]);

                // 3. Gắn sự kiện (Callback) khi người dùng click vào thẻ sách này
                cardController.setCallbacks(
                        // Hàm chạy khi click vào Thẻ sách (Mở chi tiết)
                        () -> openSidePanel(book[0], book[1], book[2], book[3]),
                        // Hàm chạy khi bấm nút Add to Cart
                        () -> System.out.println("Đã thêm vào giỏ: " + book[0])
                );

                // 4. Nhét thẻ sách vào khung chứa
                booksContainer.getChildren().add(cardNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi nạp Component Thẻ sách. Kiểm tra lại đường dẫn FXML!");
        }
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