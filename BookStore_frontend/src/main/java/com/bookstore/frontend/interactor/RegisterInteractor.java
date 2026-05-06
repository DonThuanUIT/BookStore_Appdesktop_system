package com.bookstore.frontend.interactor;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.model.RegisterModel;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class RegisterInteractor {
    private final RegisterModel model;
    private final HttpClient client = HttpClient.newHttpClient();

    public RegisterInteractor(RegisterModel model) { this.model = model; }

    public void register() {
        if (!validate()) return;

        model.loadingProperty().set(true);
        model.messageProperty().set("");

        Map<String, String> payload = Map.of(
                "username", model.usernameProperty().get(),
                "password", model.passwordProperty().get()
        );

        ApiClient.getInstance().post("/auth/register", payload)
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200 || res.statusCode() == 201) {
                        AlertUtils.show(AlertType.INFORMATION, "Registration Successful", "Welcome! Your account has been created.");
                        navigateToLogin();
                    } else {
                        model.messageProperty().set("Registration failed.");
                        AlertUtils.show(AlertType.ERROR, "Registration Failed", "Username already exists or data is invalid.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        model.messageProperty().set("Connection error.");
                        AlertUtils.show(AlertType.WARNING, "Server Error", "Cannot connect to server. Is Backend running?");
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