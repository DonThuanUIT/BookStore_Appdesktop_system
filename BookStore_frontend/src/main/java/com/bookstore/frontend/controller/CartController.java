package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.CartInteractor;
import com.bookstore.frontend.model.CartModel;
import com.bookstore.frontend.util.CartStore;
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

public class CartController extends BaseController {
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblTotalPrice;

    private final CartModel model;
    private final CartInteractor interactor;
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

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
        renderCart();
    }

    @FXML
    private void handleCheckout() {
        System.out.println("===== CHECKOUT CLICKED =====");
        if (model.getItems().isEmpty()) {
            System.out.println("Cart empty");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bookstore/frontend/view/PaymentMethodView.fxml")
            );
            VBox root = loader.load();
            PaymentController paymentCtrl = loader.getController();

            // THAY ĐỔI TẠI ĐÂY: Khớp hoàn toàn kiểu trả về CompletableFuture<Boolean> với CartInteractor
            paymentCtrl.setOnConfirm(() -> {
                String method = paymentCtrl.getSelectedMethod();
                System.out.println("FRONTEND PROCESSING ORDER VIA API FOR: " + method);

                // Gọi hàm async từ interactor và xâu chuỗi xử lý dọn giỏ hàng khi thành công
                return interactor.placeOrder(model.getItems(), method).thenApply(isSuccess -> {
                    if (isSuccess) {
                        Platform.runLater(() -> {
                            System.out.println("Đặt hàng thành công! Đang dọn giỏ hàng...");
                            model.clearCart();
                            renderCart();
                        });
                        return true;
                    }
                    return false;
                });
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Payment");
            stage.show();

            System.out.println("PAYMENT OPENED");
        } catch (Exception e) {
            System.out.println("ERROR OPENING PAYMENT:");
            e.printStackTrace();
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
                model.removeItem(item);
                renderCart();
            });

            row.getChildren().addAll(thumbView, infoBox, spacer, subtotal, removeBtn);
            cartItemsContainer.getChildren().add(row);
        });
    }
}