package com.bookstore.frontend.interactor;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.model.LoginModel;
import javafx.application.Platform;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginInteractor {
    private final LoginModel model;
    private final HttpClient client = HttpClient.newHttpClient();

    public LoginInteractor(LoginModel model) {
        this.model = model;
    }

    // Sửa lại hàm login trong LoginInteractor.java
    public void login() {
        String username = model.usernameProperty().get();
        String password = model.passwordProperty().get();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            model.messageProperty().set("Credentials cannot be empty.");
            return;
        }

        model.loadingProperty().set(true);

        // Sử dụng chuỗi JSON thuần túy để tránh lỗi format
        String json = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json") // Quan trọng để tránh lỗi 405/406
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    // Log để bạn kiểm tra trong console khi chạy
                    System.out.println("Backend Response Code: " + res.statusCode());

                    if (res.statusCode() == 200) {
                        model.messageProperty().set("Login successful!");
                    } else if (res.statusCode() == 405) {
                        model.messageProperty().set("Error 405: Method Not Allowed. Check Backend mapping.");
                    } else {
                        model.messageProperty().set("Invalid username or password.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        model.messageProperty().set("Connection failed: " + ex.getMessage());
                    });
                    return null;
                });
    }

    public void togglePassword() {
        model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get());
    }

    public void navigateToRegister() {
        try {
            MainApplication.showView("view/RegisterView.fxml", "Register");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}