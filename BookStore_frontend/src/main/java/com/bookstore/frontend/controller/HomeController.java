package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.HomeInteractor;
import com.bookstore.frontend.model.HomeModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController extends BaseController{
    @FXML private Label lblWelcome;

    private final HomeModel model;
    private final HomeInteractor interactor;

    public HomeController() {
        this.model = new HomeModel();
        this.interactor = new HomeInteractor(this.model);
    }

    @FXML
    public void initialize() {
        lblWelcome.textProperty().bind(model.welcomeMessageProperty());
    }

    @Override
    public void onNavigate(Object data) {
        String username = (data instanceof String) ? (String) data : "Test Customer";

        interactor.loadDashboardData(username);
    }
}
