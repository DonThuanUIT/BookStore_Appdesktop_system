package com.bookstore.frontend.controller;

import com.bookstore.frontend.components.AccountPopup;
import com.bookstore.frontend.components.MiniCartPopup;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.util.UserSession;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // --- UI Components ---
    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnShop, btnCart, btnInventory, btnImport, btnAccount, btnNavMiniCart;
    @FXML private Label lblCartBadge;

    // --- Custom Components ---
    private AccountPopup accountPopup;
    private MiniCartPopup miniCartPopup;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NavigationService.getInstance().setContentArea(contentArea);

        // [CODE CỦA BẠN] ĐÃ XÓA: Đoạn code Platform.runLater ép setMaximized(true) gây xung đột luồng render.
        // Việc quản lý phóng to cửa sổ giờ đây do MainApplication.showView() đảm nhận hoàn toàn.

        // [CODE ĐỒNG ĐỘI] Khởi tạo các Popup
        accountPopup = new AccountPopup();
        miniCartPopup = new MiniCartPopup();

        // [CODE ĐỒNG ĐỘI] Bindings cho giỏ hàng
        setupCartBindings();

        // [CODE ĐỒNG ĐỘI] Điều hướng & Role
        applyCustomerNavVisibilityForRole();

        // Khởi động vào trang Home và bật trạng thái Active
        navigateAndUpdateState(PageType.HOME);
    }

    private void setupCartBindings() {
        lblCartBadge.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            int n = CartStore.getInstance().getModel().totalQuantityProperty().get();
                            return n > 0 ? String.valueOf(n) : "";
                        },
                        CartStore.getInstance().getModel().totalQuantityProperty()
                )
        );
        lblCartBadge.visibleProperty().bind(CartStore.getInstance().getModel().totalQuantityProperty().greaterThan(0));
        lblCartBadge.managedProperty().bind(lblCartBadge.visibleProperty());
    }

    // --- Xử lý sự kiện Popup ---
    @FXML
    void onAccountClick() {
        if (accountPopup.isShowing()) {
            accountPopup.hide();
        } else {
            var bounds = btnAccount.localToScreen(btnAccount.getBoundsInLocal());
            accountPopup.show(btnAccount.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY() + 5);
        }
    }

    @FXML
    void onNavMiniCartClick() {
        if (miniCartPopup.isShowing()) {
            miniCartPopup.hide();
        } else {
            // [CHUẨN SOLID] Tận dụng logic Refactor của đồng đội: Giao việc render UI cho MiniCartPopup
            miniCartPopup.rebuild();
            var bounds = btnNavMiniCart.localToScreen(btnNavMiniCart.getBoundsInLocal());
            miniCartPopup.show(btnNavMiniCart.getScene().getWindow(), bounds.getMaxX() - 340, bounds.getMaxY() + 10);
        }
    }

    // --- Cụm Điều hướng (Đã tối ưu kết hợp code 2 người) ---
    @FXML void onHomeClick() { navigateAndUpdateState(PageType.HOME); }
    @FXML void onShopNavClick() { navigateAndUpdateState(PageType.SHOP); }
    @FXML void onCartClick() { navigateAndUpdateState(PageType.CART); }
    @FXML void onInventoryClick() { navigateAndUpdateState(PageType.INVENTORY); }
    @FXML void onImportClick() { navigateAndUpdateState(PageType.IMPORT); }

    private void navigateAndUpdateState(PageType pageType) {
        NavigationService.getInstance().navigateTo(pageType);
        updateNavbarActiveState(pageType);
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

    private void applyCustomerNavVisibilityForRole() {
        boolean customer = UserSession.getInstance().isCustomer();
        btnImport.setVisible(!customer); btnImport.setManaged(!customer);
        btnInventory.setVisible(!customer); btnInventory.setManaged(!customer);
    }
}