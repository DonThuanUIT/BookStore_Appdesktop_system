package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.HomeModel;

public class HomeInteractor {
    private final HomeModel model;

    public HomeInteractor(HomeModel model) {
        this.model = model;
    }

    public void loadDashboardData(String username) {
        String message = "Welcome back, " + (username != null ? username : "Customer") + "!";
        model.welcomeMessageProperty().set(message);
    }
}
