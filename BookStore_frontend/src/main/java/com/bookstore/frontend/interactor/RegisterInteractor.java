package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.RegisterModel;
import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class RegisterInteractor {
    private final RegisterModel model;
    private final HttpClient client = HttpClient.newHttpClient();

    public RegisterInteractor(RegisterModel model) {
        this.model = model;
    }

    public void executeRegister() {
        String username = model.usernameProperty().get();
        String password = model.passwordProperty().get();
        String confirm = model.confirmPasswordProperty().get();

        if (username.isBlank() || password.length() < 6) {
            AlertUtils.show(AlertType.WARNING, "Validation", "Username cannot be empty and Password must be >= 6 chars.");
            return;
        }

        if (!password.equals(confirm)) {
            AlertUtils.show(AlertType.WARNING, "Validation", "Passwords do not match!");
            return;
        }

        model.loadingProperty().set(true);

        // Gửi ĐÚNG 2 trường Backend yêu cầu trong RegistrationRequest record
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/register"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200 || res.statusCode() == 201) {
                        AlertUtils.show(AlertType.INFORMATION, "Success", "Account created successfully!");
                        navigateToLogin();
                    } else {
                        // Hiển thị message lỗi từ Backend ném ra (như "Username da ton tai!")
                        String errorMsg = res.body().isEmpty() ? "Status Code: " + res.statusCode() : res.body();
                        AlertUtils.show(AlertType.ERROR, "Registration Failed", errorMsg);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        AlertUtils.show(AlertType.ERROR, "Error", "Connection failed!");
                    });
                    return null;
                });
    }

    public void navigateToLogin() {
        try { MainApplication.showView("LoginView.fxml", "BookStore - Login"); } catch (Exception e) { e.printStackTrace(); }
    }
}