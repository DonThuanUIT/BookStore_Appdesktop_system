package com.bookstore.frontend.controller;

import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NavigationService.getInstance().setContentArea(contentArea);

        NavigationService.getInstance().navigateTo(PageType.HOME);
    }

    @FXML
    void onHomeClick() {
        NavigationService.getInstance().navigateTo(PageType.HOME);
    }

    @FXML
    void onAboutClick() {
        // Tạm thời để trống hoặc in ra log để test
        System.out.println("About clicked");
    }

    @FXML
    void onShopNavClick() {
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
        NavigationService.getInstance().navigateTo(PageType.CART);
    }
}
