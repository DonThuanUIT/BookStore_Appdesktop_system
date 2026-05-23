package com.bookstore.frontend.navigation;

public enum PageType {
    HOME("/com/bookstore/frontend/view/HomeView.fxml"),
    SHOP("/com/bookstore/frontend/view/ShopView.fxml"),
    ADMIN_DASHBOARD("/com/bookstore/frontend/view/AdminDashboard.fxml"),
    CART("/com/bookstore/frontend/view/CartView.fxml"),
    INVENTORY("/com/bookstore/frontend/view/InventoryView.fxml"),
    IMPORT("/com/bookstore/frontend/view/ImportHistoryView.fxml"),
    IMPORT_CREATE("/com/bookstore/frontend/view/ImportCreateView.fxml"),
    PROFILE("/com/bookstore/frontend/view/ProfileView.fxml"),
    ORDER_HISTORY("/com/bookstore/frontend/view/OrderHistoryView.fxml");

    private final String fxmlPath;

    PageType(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}