package com.bookstore.frontend.interactor;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.model.RegisterModel;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RegisterInteractor {
    private final RegisterModel model;
    private final HttpClient client = HttpClient.newHttpClient();

    public RegisterInteractor(RegisterModel model) { this.model = model; }

    public void register() {
        if (!validate()) return;

        model.loadingProperty().set(true);
        model.messageProperty().set("");

        String json = String.format(
                "{\"username\":\"%s\", \"password\":\"%s\", \"email\":\"%s\", \"address\":\"%s\", \"role\":\"%s\"}",
                model.usernameProperty().get(), model.passwordProperty().get(),
                model.emailProperty().get(), model.addressProperty().get(), model.roleProperty().get()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 201 || res.statusCode() == 200) {
                        // Thông báo thành công bằng Alert
                        AlertUtils.show(AlertType.INFORMATION, "Registration Successful", "Welcome! Your account has been created.");
                        navigateToLogin();
                    } else {
                        String errMsg = "Username already exists or data is invalid.";
                        model.messageProperty().set(errMsg);
                        AlertUtils.show(AlertType.ERROR, "Registration Failed", errMsg);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        String errMsg = "Cannot connect to server. Please check if your backend is running.";
                        model.messageProperty().set("Connection error.");
                        AlertUtils.show(AlertType.WARNING, "Server Error", errMsg);
                    });
                    return null;
                });
    }

    private boolean validate() {
        if (model.usernameProperty().get().isBlank() || model.passwordProperty().get().isBlank()) {
            AlertUtils.show(AlertType.WARNING, "Validation Error", "Username and Password cannot be empty!");
            return false;
        }
        if (!model.passwordProperty().get().equals(model.confirmPasswordProperty().get())) {
            AlertUtils.show(AlertType.WARNING, "Validation Error", "Confirmation password does not match!");
            return false;
        }
        return true;
    }

    public void navigateToLogin() {
        try { MainApplication.showView("LoginView.fxml", "Login - BookStore"); }
        catch (Exception e) { e.printStackTrace(); }
    }
}