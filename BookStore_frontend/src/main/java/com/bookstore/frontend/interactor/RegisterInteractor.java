package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.RegisterModel;
import com.bookstore.frontend.MainApplication;
import javafx.application.Platform;
import java.net.URI;
import java.net.http.*;

public class RegisterInteractor {
    private final RegisterModel model;
    private final HttpClient client = HttpClient.newHttpClient();

    public RegisterInteractor(RegisterModel model) { this.model = model; }

    public void goToLogin() {
        try {
            // Chỉ truyền tên file vì MainApplication đã có sẵn VIEW_BASE_PATH
            MainApplication.showView("LoginView.fxml", "BookStore - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register() {
        if (!model.passwordProperty().get().equals(model.confirmPasswordProperty().get())) {
            model.messageProperty().set("Passwords do not match!");
            return;
        }

        model.loadingProperty().set(true);
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}",
                model.usernameProperty().get(), model.passwordProperty().get());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200) {
                        model.messageProperty().set("Registration successful! Please login.");
                    } else {
                        model.messageProperty().set("Error: " + res.body());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> model.messageProperty().set("Cannot connect to server."));
                    return null;
                });
    }
}