package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.CartInteractor;
import com.bookstore.frontend.model.CartItemDTO;
import com.bookstore.frontend.model.CartModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CartController extends BaseController{
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblTotalPrice;

    private final CartModel model;
    private final CartInteractor interactor;

    public CartController() {
        this.model = new CartModel();
        this.interactor = new CartInteractor(this.model);
    }

    @FXML
    public void initialize() {
        // Binding: Tự động cập nhật nhãn tổng tiền khi Model thay đổi
        model.totalPriceProperty().addListener((obs, oldVal, newVal) -> {
            lblTotalPrice.setText(String.format("Rs. %.2f", newVal.doubleValue()));
        });
    }

    @Override
    public void onNavigate(Object data) {
        interactor.loadCartItems();
        renderCart();
    }

    private void renderCart() {
        cartItemsContainer.getChildren().clear();
        for (CartItemDTO item : model.getItems()) {
            cartItemsContainer.getChildren().add(createCartItemRow(item));
        }
    }

    /**
     * Tạo một hàng sản phẩm trong giỏ hàng (giống Card trong ảnh thiết kế)
     */
    private HBox createCartItemRow(CartItemDTO item) {
        HBox row = new HBox(30);
        row.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0; -fx-padding: 20; -fx-alignment: CENTER_LEFT;");

        // 1. Ảnh giả lập
        VBox imgBox = new VBox();
        imgBox.setPrefSize(100, 140);
        imgBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd;");

        // 2. Thông tin sách
        VBox infoBox = new VBox(5);
        Label title = new Label(item.getBook().getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        Label author = new Label(item.getBook().getAuthor());
        Label price = new Label(item.getBook().getPrice());
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-accent-gold;");
        infoBox.getChildren().addAll(title, author, price);

        // 3. Spinner chọn số lượng (Gắn kết trực tiếp với Model)
        VBox actionBox = new VBox(10);
        Label lblCopies = new Label("Copies");
        Spinner<Integer> spinner = new Spinner<>(1, 100, item.getQuantity());
        spinner.setPrefWidth(80);

        // Cập nhật Model ngay khi người dùng thay đổi Spinner
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            item.setQuantity(newVal);
        });

        actionBox.getChildren().addAll(lblCopies, spinner);

        row.getChildren().addAll(imgBox, infoBox, actionBox);
        return row;
    }
}
