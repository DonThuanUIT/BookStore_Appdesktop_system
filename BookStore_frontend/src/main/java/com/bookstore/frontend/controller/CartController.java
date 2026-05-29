package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.CartInteractor;
import com.bookstore.frontend.model.CartModel;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.util.OrderStatusStore;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import com.bookstore.frontend.service.api.BookApiService;
import java.util.ArrayList;
import java.util.List;

public class CartController extends BaseController {
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblTotalPrice;
    @FXML private ImageView imgRandomBook;

    private final CartModel model;
    private final CartInteractor interactor;
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    private final BookApiService bookApiService = new BookApiService();
    private Timeline randomImageTimeline;
    private final List<String> bookImageUrls = new ArrayList<>();


    public CartController() {
        this.model = CartStore.getInstance().getModel();
        // TRUYỀN THAM SỐ VÀO ĐÂY ĐỂ HẾT BÁO ĐỎ:
        this.interactor = new CartInteractor(this.model);
    }

    @Override
    public void onNavigate(Object data) {
        model.refreshAggregates();
        renderCart();
    }

    @FXML
    public void initialize() {
        if (lblTotalPrice != null) {
            lblTotalPrice.setText(String.format("%.2fđ", model.totalPriceProperty().get()));
            model.totalPriceProperty().addListener((obs, oldVal, newVal) ->
                    lblTotalPrice.setText(String.format("%.2fđ", newVal.doubleValue())));
        }

        // Random book cover sidebar (BE)
        if (imgRandomBook != null) {
            imgRandomBook.setImage(new Image(DEFAULT_COVER_URL, true));
            startRandomSidebar();
        }

        renderCart();
    }

    @FXML
    private void handleCheckout() {
        // 1. Kiểm tra giỏ hàng trống bằng AlertUtils
        if (model.getItems().isEmpty()) {
            AlertUtils.show(Alert.AlertType.WARNING, "Giỏ hàng trống",
                    "Giỏ hàng của bạn đang trống. Vui lòng thêm sách trước khi thanh toán!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bookstore/frontend/view/PaymentMethodView.fxml")
            );
            VBox root = loader.load();
            PaymentController paymentCtrl = loader.getController();

            paymentCtrl.setOnConfirm(() -> {
                String method = paymentCtrl.getSelectedMethod();

                // Xử lý đặt hàng
                return interactor.placeOrder(model.getItems(), method).thenApply(isSuccess -> {
                    if (isSuccess) {
                        Platform.runLater(() -> {
                            // 2. Thông báo thành công
                            AlertUtils.show(Alert.AlertType.INFORMATION, "Thành công",
                                    "Đơn hàng của bạn đã được đặt thành công!");
                            // TĂNG SỐ ĐƠN CHỜ DUYỆT
                            OrderStatusStore.getInstance().incrementPendingOrder();
                            model.clearCart();
                            renderCart();
                        });
                        return true;
                    } else {
                        // 3. Thông báo lỗi đặt hàng
                        Platform.runLater(() ->
                                AlertUtils.show(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi xử lý đơn hàng.")
                        );
                        return false;
                    }
                });
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Phương thức thanh toán");
            stage.show();

        } catch (Exception e) {
            // 4. Thông báo lỗi hệ thống
            AlertUtils.show(Alert.AlertType.ERROR, "Lỗi hệ thống",
                    "Không thể mở cửa sổ thanh toán: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startRandomSidebar() {
        // prevent multiple timelines
        if (randomImageTimeline != null) {
            randomImageTimeline.stop();
            randomImageTimeline = null;
        }
        bookImageUrls.clear();

        bookApiService.fetchBooks(0, 50)
                .thenAccept(pageData -> {
                    try {
                        System.out.println("[CartController] fetchBooks(0,50) status ok, pageData=" + (pageData != null));
                        if (pageData == null || pageData.getContent() == null) {
                            Platform.runLater(() -> {
                                bookImageUrls.clear();
                                updateRandomImage();
                            });
                            return;
                        }

                        List<String> urls = pageData.getContent().stream()
                                .map(b -> b.getImageUrl())
                                .filter(u -> u != null && !u.isBlank())
                                .toList();

                        System.out.println("[CartController] imageUrl count=" + urls.size());
                        if (!urls.isEmpty()) {
                            System.out.println("[CartController] sample urls: " + urls.subList(0, Math.min(5, urls.size())));
                        }

                        Platform.runLater(() -> {
                            bookImageUrls.clear();
                            bookImageUrls.addAll(urls);
                            updateRandomImage();

                            randomImageTimeline = new Timeline(
                                    new KeyFrame(Duration.seconds(5), e -> updateRandomImage())
                            );
                            randomImageTimeline.setCycleCount(Timeline.INDEFINITE);
                            randomImageTimeline.play();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> updateRandomImage());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("[CartController] fetchBooks failed: " + ex);
                    ex.printStackTrace();
                    Platform.runLater(() -> updateRandomImage());
                    return null;
                });
    }

    private void updateRandomImage() {
        if (imgRandomBook == null) return;

        String url;
        if (bookImageUrls.isEmpty()) {
            url = DEFAULT_COVER_URL;
        } else {
            int idx = (int) (Math.random() * bookImageUrls.size());
            url = bookImageUrls.get(idx);
            if (url == null || url.isBlank()) url = DEFAULT_COVER_URL;
        }

        try {
            imgRandomBook.setImage(new Image(url, true));
        } catch (Exception e) {
            imgRandomBook.setImage(new Image(DEFAULT_COVER_URL, true));
        }
    }

    private void renderCart() {

        cartItemsContainer.getChildren().clear();
        model.refreshAggregates();

        if (model.getItems().isEmpty()) {
            Label empty = new Label("Your cart is empty");
            empty.getStyleClass().add("payment-method-sub");
            cartItemsContainer.getChildren().add(empty);
            return;
        }

        model.getItems().forEach(item -> {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("payment-method-row");
            row.setStyle("-fx-padding: 10;");

            ImageView thumbView = new ImageView();
            thumbView.setFitWidth(50);
            thumbView.setFitHeight(70);
            thumbView.setPreserveRatio(true);

            String imgUrl = (item.getBook().getImageUrl() != null && !item.getBook().getImageUrl().isBlank())
                    ? item.getBook().getImageUrl()
                    : DEFAULT_COVER_URL;
            try {
                thumbView.setImage(new Image(imgUrl, true));
            } catch (Exception e) {
                thumbView.setImage(new Image(DEFAULT_COVER_URL));
            }

            VBox infoBox = new VBox(4);
            Label title = new Label(item.getBook().getTitle());
            title.getStyleClass().add("payment-method-title");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            Label author = new Label("Tác giả: " + item.getBook().getFormattedAuthors());
            author.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

            Label qtyAndPrice = new Label(
                    String.format("Số lượng: %d  ×  $%.2f", item.getQuantity(), item.getBook().getPrice())
            );
            qtyAndPrice.getStyleClass().add("payment-method-sub");

            infoBox.getChildren().addAll(title, author, qtyAndPrice);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label subtotal = new Label(String.format("$%.2f", item.getSubtotal()));
            subtotal.getStyleClass().add("book-card-price");
            subtotal.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            Button removeBtn = new Button("Remove");
            removeBtn.getStyleClass().add("btn-huy-payment");
            removeBtn.setOnAction(e -> {
                boolean confirmed = AlertUtils.confirm("Xác nhận xóa",
                        "Bạn có chắc muốn xóa '" + item.getBook().getTitle() + "' khỏi giỏ hàng?");
                if(confirmed) {
                    model.removeItem(item);
                    renderCart();
                }
            });

            row.getChildren().addAll(thumbView, infoBox, spacer, subtotal, removeBtn);
            cartItemsContainer.getChildren().add(row);
        });
    }
}