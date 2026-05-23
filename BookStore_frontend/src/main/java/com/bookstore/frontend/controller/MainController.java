package com.bookstore.frontend.controller;

import com.bookstore.frontend.components.AccountPopup;
import com.bookstore.frontend.components.MiniCartPopup;
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
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

        // Khởi tạo các Popup
        accountPopup = new AccountPopup();
        miniCartPopup = new MiniCartPopup();

        // Bindings cho giỏ hàng
        setupCartBindings();

        // Điều hướng & Role
        applyCustomerNavVisibilityForRole();
        NavigationService.getInstance().navigateTo(PageType.HOME);
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

    // --- Xử lý sự kiện Account ---
    @FXML
    void onAccountClick() {
        if (accountPopup.isShowing()) {
            accountPopup.hide();
        } else {
            // Lấy tọa độ nút User và hiển thị ngay dưới nó
            var bounds = btnAccount.localToScreen(btnAccount.getBoundsInLocal());
            accountPopup.show(btnAccount.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY() + 5);
        }
    }

    // --- Xử lý sự kiện MiniCart ---
    @FXML
    void onNavMiniCartClick() {
        if (miniCartPopup.isShowing()) {
            miniCartPopup.hide();
        } else {
            miniCartPopup.rebuild(); // Gọi hàm rebuild đã được chuyển sang
            var bounds = btnNavMiniCart.localToScreen(btnNavMiniCart.getBoundsInLocal());
            miniCartPopup.show(btnNavMiniCart.getScene().getWindow(), bounds.getMaxX() - 340, bounds.getMaxY() + 10);
        }
    }

    // --- Điều hướng ---
    @FXML void onHomeClick() { NavigationService.getInstance().navigateTo(PageType.HOME); }
    @FXML void onShopNavClick() { NavigationService.getInstance().navigateTo(PageType.SHOP); }
    @FXML void onCartClick() { NavigationService.getInstance().navigateTo(PageType.CART); }
    @FXML void onInventoryClick() { NavigationService.getInstance().navigateTo(PageType.INVENTORY); }
    @FXML void onImportClick() { NavigationService.getInstance().navigateTo(PageType.IMPORT); }

    private void applyCustomerNavVisibilityForRole() {
        boolean customer = UserSession.getInstance().isCustomer();
        btnImport.setVisible(!customer); btnImport.setManaged(!customer);
        btnInventory.setVisible(!customer); btnInventory.setManaged(!customer);
    }
}