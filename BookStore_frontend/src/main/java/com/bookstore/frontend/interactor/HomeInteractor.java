package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.HomeModel;

public class HomeInteractor {
    private final HomeModel model;

    public HomeInteractor(HomeModel model) {
        this.model = model;
    }

    /**
     * Giả lập việc lấy dữ liệu từ Backend, sau này làm chính thức thì bổ sung
     */
    public void loadDashboardData(String username) {
        // call API
        String message = "Well com back, " + (username != null ? username : "Customer") + "!";
        model.welcomeMessageProperty().set(message);
    }
}
