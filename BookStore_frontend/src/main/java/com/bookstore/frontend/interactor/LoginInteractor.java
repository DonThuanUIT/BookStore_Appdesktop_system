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

    public void login() {
        model.loadingProperty().set(true);
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}",
                model.usernameProperty().get(), model.passwordProperty().get());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200) {
                        model.messageProperty().set("Login Success!");
                    } else {
                        model.messageProperty().set("Invalid credentials!");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        model.messageProperty().set("Server connection failed.");
                    });
                    return null;
                });
    }

    public void togglePasswordVisibility() {
        model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get());
    }

    // Logic chuyển trang
    public void navigateToRegister() {
        try {
            MainApplication.showView("RegisterView.fxml", "BookStore - Register");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}