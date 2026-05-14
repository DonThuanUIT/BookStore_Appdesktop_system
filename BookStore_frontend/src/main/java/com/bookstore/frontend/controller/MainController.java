package com.bookstore.frontend.controller;

import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import javafx.application.Platform;
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
    @FXML private Button btnImport;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        NavigationService.getInstance().setContentArea(contentArea);

        NavigationService.getInstance().currentPageProperty().addListener((obs, oldPage, newPage) -> {
            Platform.runLater(() -> {
                updateNavbarActiveState(newPage);
            });
        });


        NavigationService.getInstance().navigateTo(PageType.HOME);
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