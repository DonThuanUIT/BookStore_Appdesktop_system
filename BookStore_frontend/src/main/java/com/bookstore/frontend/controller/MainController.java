package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.CartModel;
import com.bookstore.frontend.model.dto.CartItemDTO;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.util.UserSession;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private StackPane contentArea;

    @FXML private Button btnHome;
    @FXML private Button btnShop;
    @FXML private Button btnCart;
    @FXML private Button btnInventory;
    @FXML private Button btnImport;
    @FXML private Label lblCartBadge;
    @FXML private Button btnNavMiniCart;

    private Popup miniCartPopup;
    private VBox miniCartShell;
    private VBox miniCartBody;

    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NavigationService.getInstance().setContentArea(contentArea);

        // ĐÃ XÓA: Đoạn code Platform.runLater ép setMaximized(true) gây xung đột luồng render.
        // Việc quản lý phóng to cửa sổ giờ đây do MainApplication.showView() đảm nhận hoàn toàn.

        lblCartBadge.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            int n = CartStore.getInstance().getModel().totalQuantityProperty().get();
                            return n > 0 ? String.valueOf(n) : "";
                        },
                        CartStore.getInstance().getModel().totalQuantityProperty()
                )
        );
        lblCartBadge.visibleProperty().bind(
                CartStore.getInstance().getModel().totalQuantityProperty().greaterThan(0)
        );
        lblCartBadge.managedProperty().bind(lblCartBadge.visibleProperty());

        setupMiniCartPopup();
        registerCartChangeRefresh();

        NavigationService.getInstance().currentPageProperty().addListener((obs, oldPage, newPage) -> {
            Platform.runLater(() -> {
                updateNavbarActiveState(newPage);
            });
        });

        applyCustomerNavVisibilityForRole();

        NavigationService.getInstance().navigateTo(PageType.HOME);
    }

    private void setupMiniCartPopup() {
        miniCartPopup = new Popup();
        miniCartPopup.setAutoHide(true);

        miniCartShell = new VBox(0);
        miniCartShell.setPrefWidth(340);
        miniCartShell.setMinWidth(320);
        miniCartShell.setMaxWidth(360);
        miniCartShell.setMinHeight(180);
        miniCartShell.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1e2430, #151a22);"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-color: rgba(255,199,7,0.35);"
                        + "-fx-border-radius: 16;"
                        + "-fx-border-width: 1;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 24, 0.2, 0, 8);"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 18, 12, 18));

        Label title = new Label("Giỏ hàng");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sub = new Label("Xem nhanh mặt hàng");
        sub.setStyle("-fx-text-fill: #9aa5b5; -fx-font-size: 12px;");

        VBox titles = new VBox(2, title, sub);
        header.getChildren().addAll(titles, spacer);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.08);");

        miniCartBody = new VBox(10);
        miniCartBody.setPadding(new Insets(8, 12, 8, 12));

        ScrollPane scroll = new ScrollPane(miniCartBody);
        scroll.setFitToWidth(true);
        scroll.setMinViewportHeight(120);
        scroll.setPrefViewportHeight(220);
        scroll.setMaxHeight(320);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.getContent().setStyle("-fx-background-color: transparent;");

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: rgba(255,255,255,0.08);");

        Button btnOpenCart = new Button("Mở giỏ đầy đủ");
        btnOpenCart.setMaxWidth(Double.MAX_VALUE);
        btnOpenCart.getStyleClass().add("btn-primary");
        btnOpenCart.setStyle(btnOpenCart.getStyle() + "-fx-background-radius: 10; -fx-padding: 12 16;");
        btnOpenCart.setOnAction(e -> {
            miniCartPopup.hide();
            NavigationService.getInstance().navigateTo(PageType.CART);
        });

        VBox footer = new VBox(10, btnOpenCart);
        footer.setPadding(new Insets(12, 16, 16, 16));

        miniCartShell.getChildren().addAll(header, sep, scroll, sep2, footer);
        miniCartPopup.getContent().add(miniCartShell);
    }

    private void registerCartChangeRefresh() {
        CartStore.getInstance().getModel().getItems().addListener((ListChangeListener<CartItemDTO>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (CartItemDTO added : c.getAddedSubList()) {
                        added.quantityProperty().addListener((obs, ov, nv) -> refreshMiniCartIfOpen());
                    }
                }
            }
            refreshMiniCartIfOpen();
        });
        for (CartItemDTO item : CartStore.getInstance().getModel().getItems()) {
            item.quantityProperty().addListener((obs, ov, nv) -> refreshMiniCartIfOpen());
        }
    }

    private void refreshMiniCartIfOpen() {
        if (miniCartPopup != null && miniCartPopup.isShowing()) {
            rebuildMiniCartRows();
        }
    }

    private void rebuildMiniCartRows() {
        miniCartBody.getChildren().clear();
        CartModel cart = CartStore.getInstance().getModel();
        if (cart.getItems().isEmpty()) {
            Label empty = new Label("Giỏ hàng đang trống.\nThêm sách từ New Arrivals hoặc Shop.");
            empty.setWrapText(true);
            empty.setStyle("-fx-text-fill: #9aa5b5; -fx-font-size: 13px; -fx-padding: 16 8 8 8;");
            Hyperlink goShop = new Hyperlink("Đi tới Shop →");
            goShop.setStyle("-fx-text-fill: #FFC107; -fx-font-size: 14px; -fx-border-width: 0;");
            goShop.setOnAction(e -> {
                miniCartPopup.hide();
                NavigationService.getInstance().navigateTo(PageType.SHOP);
            });
            miniCartBody.getChildren().addAll(empty, goShop);
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

        ImageView img = new ImageView();
        img.setFitWidth(46);
        img.setFitHeight(64);
        img.setPreserveRatio(true);
        String url = (item.getBook().getImageUrl() != null && !item.getBook().getImageUrl().isBlank())
                ? item.getBook().getImageUrl()
                : DEFAULT_COVER_URL;
        img.setImage(new Image(url, true));

        Rectangle clip = new Rectangle(46, 64);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        img.setClip(clip);

        VBox textCol = new VBox(4);
        HBox.setHgrow(textCol, Priority.ALWAYS);
        textCol.setMinWidth(0);

        Label name = new Label(item.getBook().getTitle());
        name.setWrapText(false);
        name.setTextOverrun(OverrunStyle.ELLIPSIS);
        name.maxWidthProperty().bind(textCol.widthProperty());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label meta = new Label(String.format("SL %d  ·  %,.0f đ", item.getQuantity(), item.getBook().getPrice()));
        meta.setWrapText(false);
        meta.setTextOverrun(OverrunStyle.ELLIPSIS);
        meta.maxWidthProperty().bind(textCol.widthProperty());
        meta.setStyle("-fx-text-fill: #c7ced6; -fx-font-size: 12px;");

        textCol.getChildren().addAll(name, meta);

        Button btnRemove = new Button("Hủy");
        btnRemove.setMnemonicParsing(false);
        btnRemove.setFocusTraversable(false);
        final double btnW = 56;
        btnRemove.setMinWidth(btnW);
        btnRemove.setPrefWidth(btnW);
        btnRemove.setMaxWidth(btnW);
        HBox.setHgrow(btnRemove, Priority.NEVER);
        btnRemove.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: #ff8a80;"
                        + "-fx-border-color: rgba(255,138,128,0.6);"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 6 10;"
                        + "-fx-cursor: hand;"
                        + "-fx-font-size: 12px;"
        );
        btnRemove.setOnAction(ev -> {
            ev.consume();
            CartStore.getInstance().removeItem(item);
            rebuildMiniCartRows();
        });

        row.getChildren().addAll(img, textCol, btnRemove);
        return row;
    }

    @FXML
    void onNavMiniCartClick() {
        if (miniCartPopup.isShowing()) {
            miniCartPopup.hide();
            return;
        }
        rebuildMiniCartRows();
        Window owner = btnNavMiniCart.getScene() != null ? btnNavMiniCart.getScene().getWindow() : null;
        if (owner == null) {
            return;
        }
        Platform.runLater(() -> {
            miniCartPopup.show(owner);
            miniCartShell.applyCss();
            miniCartShell.layout();
            Platform.runLater(this::positionMiniCartPopup);
        });
    }

    private void positionMiniCartPopup() {
        if (!miniCartPopup.isShowing() || btnNavMiniCart.getScene() == null) {
            return;
        }
        javafx.geometry.Bounds b = btnNavMiniCart.localToScreen(btnNavMiniCart.getBoundsInLocal());
        if (b == null) {
            return;
        }
        double w = miniCartPopup.getWidth() > 40 ? miniCartPopup.getWidth() : 340;
        double h = miniCartPopup.getHeight() > 40 ? miniCartPopup.getHeight() : 300;
        double margin = 10;

        double x = b.getMaxX() - w;
        double y = b.getMaxY() + margin;

        javafx.geometry.Rectangle2D vis = Screen.getPrimary().getVisualBounds();
        x = Math.max(vis.getMinX() + 8, Math.min(x, vis.getMaxX() - w - 8));
        if (y + h > vis.getMaxY() - 8) {
            y = b.getMinY() - h - margin;
        }
        if (y < vis.getMinY() + 8) {
            y = vis.getMinY() + 8;
        }

        miniCartPopup.setX(x);
        miniCartPopup.setY(y);
    }

    private void applyCustomerNavVisibilityForRole() {
        boolean customer = UserSession.getInstance().isCustomer();
        btnImport.setVisible(!customer);
        btnImport.setManaged(!customer);
        btnInventory.setVisible(!customer);
        btnInventory.setManaged(!customer);
    }

    private void updateNavbarActiveState(PageType pageType) {
        btnHome.getStyleClass().remove("nav-button-active");
        btnShop.getStyleClass().remove("nav-button-active");
        btnCart.getStyleClass().remove("nav-button-active");
        btnInventory.getStyleClass().remove("nav-button-active");
        btnImport.getStyleClass().remove("nav-button-active");

        if (pageType == null) return;

        switch (pageType) {
            case HOME -> btnHome.getStyleClass().add("nav-button-active");
            case SHOP -> btnShop.getStyleClass().add("nav-button-active");
            case CART -> btnCart.getStyleClass().add("nav-button-active");
            case INVENTORY -> btnInventory.getStyleClass().add("nav-button-active");
            case IMPORT -> btnImport.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    void onHomeClick() {
        NavigationService.getInstance().navigateTo(PageType.HOME);
    }

    @FXML
    void onShopNavClick() {
        NavigationService.getInstance().navigateTo(PageType.SHOP);
    }

    @FXML
    void onCartClick() {
        NavigationService.getInstance().navigateTo(PageType.CART);
    }

    @FXML
    void onInventoryClick() {
        NavigationService.getInstance().navigateTo(PageType.INVENTORY);
    }

    @FXML
    void onImportClick() {
        NavigationService.getInstance().navigateTo(PageType.IMPORT);
    }
}