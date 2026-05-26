package com.bookstore.frontend.interactor;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.model.LoginModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.utils.AlertUtils;
import com.bookstore.frontend.util.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginInteractor {
    private final LoginModel model;

    public LoginInteractor(LoginModel model) { this.model = model; }

    public void login() {
        String user = model.usernameProperty().get();
        String pass = model.passwordProperty().get();

        if (user.isEmpty() || pass.isEmpty()) {
            AlertUtils.show(AlertType.WARNING, "Input Required", "Please enter both username and password.");
            return;
        }

        model.loadingProperty().set(true);
        model.messageProperty().set("");

        Map<String, String> payload = Map.of("username", user, "password", pass);

        ApiClient.getInstance().post("/auth/login", payload)
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200) {
                        try {
                            JsonNode jsonNode = ApiClient.getInstance().getMapper().readTree(res.body());
                            String token = jsonNode.get("token").asText();

                            List<String> roles = new ArrayList<>();
                            JsonNode rolesNode = jsonNode.get("roles");
                            if (rolesNode != null && rolesNode.isArray()) {
                                for (JsonNode r : rolesNode) {
                                    if (r != null && !r.isNull()) {
                                        roles.add(r.asText());
                                    }
                                }
                            }

                            UserSession.getInstance().init(token, user, roles);
                            System.out.println("Login thành công. Token: " + token);

                            ApiClient.getInstance().startSseConnection();

                            navigateToHome(user);

                        } catch (Exception e) {
                            e.printStackTrace();
                            AlertUtils.show(AlertType.ERROR, "System Error", "Lỗi xử lý phản hồi từ server.");
                        }
                    } else {
                        model.messageProperty().set("Invalid username or password.");
                        AlertUtils.show(AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        model.messageProperty().set("Server connection timed out.");
                        AlertUtils.show(AlertType.WARNING, "Connection Issue", "Could not reach the server.");
                    });
                    return null;
                });
    }

    private void navigateToHome(String username) {
        try {
            NavigationService.getInstance().clearCache();
            MainApplication.showView("MainLayout.fxml", "Neth BookPoint");
            NavigationService.getInstance().navigateTo(PageType.HOME, username);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void navigateToRegister() {
        try { MainApplication.showView("RegisterView.fxml", "Register - BookStore"); }
        catch (Exception e) { e.printStackTrace(); }
    }
    public void navigateToForgotPassword() {
        try { MainApplication.showView("ForgotPasswordView.fxml", "Forgot Password - BookStore"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void togglePasswordVisibility() {
        model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get());
    }
}