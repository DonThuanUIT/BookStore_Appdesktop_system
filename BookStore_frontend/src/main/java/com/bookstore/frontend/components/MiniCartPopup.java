package com.bookstore.frontend.components;

import com.bookstore.frontend.model.CartModel;
import com.bookstore.frontend.model.dto.CartItemDTO;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.util.CartStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;

public class MiniCartPopup extends Popup {
    private final VBox miniCartBody = new VBox(10);
    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public MiniCartPopup() {
        this.setAutoHide(true);
        VBox shell = new VBox(0);
        shell.setPrefWidth(340);
        shell.setStyle("-fx-background-color: linear-gradient(to bottom, #1e2430, #151a22);"
                + "-fx-background-radius: 16; -fx-border-color: rgba(255,199,7,0.35);"
                + "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 24, 0.2, 0, 8);");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 18, 12, 18));
        header.getChildren().add(new Label("Giỏ hàng") {{
            setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        }});

        // Scroll
        ScrollPane scroll = new ScrollPane(miniCartBody);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(220);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        miniCartBody.setPadding(new Insets(8, 12, 8, 12));

        // Footer
        Button btnOpenCart = new Button("Mở giỏ đầy đủ");
        btnOpenCart.setMaxWidth(Double.MAX_VALUE);
        btnOpenCart.setStyle("-fx-background-color: #FFCC00; -fx-text-fill: #110000; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12;");
        btnOpenCart.setOnAction(e -> {
            this.hide();
            NavigationService.getInstance().navigateTo(PageType.CART);
        });

        shell.getChildren().addAll(header, new Separator(), scroll, new Separator(), new VBox(btnOpenCart) {{ setPadding(new Insets(12, 16, 16, 16)); }});
        this.getContent().add(shell);
    }

    // Logic rebuild chuyển từ MainController sang
    public void rebuild() {
        miniCartBody.getChildren().clear();
        CartModel cart = CartStore.getInstance().getModel();

        if (cart.getItems().isEmpty()) {
            miniCartBody.getChildren().add(new Label("Giỏ hàng đang trống.") {{
                setStyle("-fx-text-fill: #9aa5b5; -fx-padding: 20;");
            }});
            return;
        }
        for (CartItemDTO item : cart.getItems()) {
            miniCartBody.getChildren().add(buildMiniCartRow(item));
        }
    }

    private Node buildMiniCartRow(CartItemDTO item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 12;");

        ImageView img = new ImageView(new Image(item.getBook().getImageUrl() != null ? item.getBook().getImageUrl() : DEFAULT_COVER_URL, true));
        img.setFitWidth(46); img.setFitHeight(64);
        img.setClip(new Rectangle(46, 64) {{ setArcWidth(8); setArcHeight(8); }});

        VBox textCol = new VBox(4);
        HBox.setHgrow(textCol, Priority.ALWAYS);
        textCol.getChildren().addAll(
                new Label(item.getBook().getTitle()) {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }},
                new Label(String.format("SL %d  ·  $%.2f", item.getQuantity(), item.getBook().getPrice())) {{ setStyle("-fx-text-fill: #c7ced6;"); }}
        );

        Button btnRemove = new Button("Hủy");
        btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff8a80; -fx-border-color: rgba(255,138,128,0.6); -fx-border-radius: 8;");
        btnRemove.setOnAction(e -> {
            CartStore.getInstance().removeItem(item);
            rebuild();
        });

        row.getChildren().addAll(img, textCol, btnRemove);
        return row;
    }
}