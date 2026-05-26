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

    @FXML private StackPane contentArea;
    @FXML private Button btnHome, btnShop, btnCart, btnInventory, btnImport, btnAccount, btnNavMiniCart, btnRevenue;
    @FXML private Label lblCartBadge;

    private AccountPopup accountPopup;
    private MiniCartPopup miniCartPopup;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NavigationService.getInstance().setContentArea(contentArea);

        accountPopup = new AccountPopup();
        miniCartPopup = new MiniCartPopup();

        setupCartBindings();

        applyRoleBasedNavVisibility();

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
            miniCartPopup.rebuild();
            var bounds = btnNavMiniCart.localToScreen(btnNavMiniCart.getBoundsInLocal());
            miniCartPopup.show(btnNavMiniCart.getScene().getWindow(), bounds.getMaxX() - 340, bounds.getMaxY() + 10);
        }
    }

    @FXML void onHomeClick() { navigateAndUpdateState(PageType.HOME); }
    @FXML void onShopNavClick() { navigateAndUpdateState(PageType.SHOP); }
    @FXML void onCartClick() { navigateAndUpdateState(PageType.CART); }
    @FXML void onInventoryClick() {
        if (!ensureVendorAccess()) return;
        navigateAndUpdateState(PageType.INVENTORY);
    }

    @FXML void onImportClick() {
        if (!ensureVendorAccess()) return;
        navigateAndUpdateState(PageType.IMPORT);
    }

    @FXML void onRevenueClick() {
        if(!ensureVendorAccess()) return;
        navigateAndUpdateState(PageType.REVENUE_REPORT);
    }
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
        btnRevenue.getStyleClass().remove("nav-button-active");

        if (pageType == null) return;

        switch (pageType) {
            case HOME -> btnHome.getStyleClass().add("nav-button-active");
            case SHOP -> btnShop.getStyleClass().add("nav-button-active");
            case CART -> btnCart.getStyleClass().add("nav-button-active");
            case INVENTORY -> btnInventory.getStyleClass().add("nav-button-active");
            case IMPORT -> btnImport.getStyleClass().add("nav-button-active");
            case REVENUE_REPORT -> btnRevenue.getStyleClass().add("nav-button-active");
        }
    }

    /** Chỉ Admin (Vendor) thấy quản lý kho và nhập hàng; Customer chỉ mua sắm. */
    public void applyRoleBasedNavVisibility() {
        boolean vendor = UserSession.getInstance().isAdmin();
        btnImport.setVisible(vendor);
        btnImport.setManaged(vendor);
        btnInventory.setVisible(vendor);
        btnInventory.setManaged(vendor);
        btnRevenue.setVisible(vendor);
        btnRevenue.setManaged(vendor);
    }

    private boolean ensureVendorAccess() {
        applyRoleBasedNavVisibility();
        if (!UserSession.getInstance().isAdmin()) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "Chỉ tài khoản Admin (Người bán) mới truy cập được tính năng này."
            ).show();
            return false;
        }
        return true;
    }
}