package com.bookstore.frontend.controller;

import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private StackPane contentArea;

    @FXML private Button btnHome;
    @FXML private Button btnShop;
    @FXML private Button btnCart;
    @FXML private Button btnInventory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NavigationService.getInstance().setContentArea(contentArea);

        //NavigationService.getInstance().navigateTo(PageType.HOME);
        setActiveButton(btnHome);
    }

    private void setActiveButton(Button activeButton) {
        // Gỡ trạng thái active của tất cả
        btnHome.getStyleClass().remove("nav-button-active");
        btnShop.getStyleClass().remove("nav-button-active");
        btnCart.getStyleClass().remove("nav-button-active");
        btnInventory.getStyleClass().remove("nav-button-active");

        // Gắn trạng thái active cho nút được chọn
        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    void onHomeClick() {
        setActiveButton(btnHome);
        NavigationService.getInstance().navigateTo(PageType.HOME);
    }

    @FXML
    void onAboutClick() {
        // Tạm thời để trống hoặc in ra log để test
        System.out.println("About clicked");
    }

    @FXML
    void onShopNavClick() {
        setActiveButton(btnShop);
        NavigationService.getInstance().navigateTo(PageType.SHOP);
    }

    @FXML
    void onDeliveryClick() {
        System.out.println("Delivery clicked");
    }

    @FXML
    void onSellersClick() {
        System.out.println("Sellers clicked");
    }
    @FXML
    void onCartClick() {
        setActiveButton(btnCart);
        NavigationService.getInstance().navigateTo(PageType.CART);
    }
    @FXML
    void onInventoryClick() {
        setActiveButton(btnInventory);
        NavigationService.getInstance().navigateTo(PageType.INVENTORY);
    }
}
