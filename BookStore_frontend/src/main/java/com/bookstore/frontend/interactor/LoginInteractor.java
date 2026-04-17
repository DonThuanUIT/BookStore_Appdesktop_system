package com.bookstore.frontend.interactor;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.model.LoginModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
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
        String username = model.usernameProperty().get();
        String password = model.passwordProperty().get();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            AlertUtils.show(AlertType.WARNING, "Login Warning", "Please enter both username and password!");
            return;
        }

        model.loadingProperty().set(true);
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200) {
                        AlertUtils.show(AlertType.INFORMATION, "Success", "Login Successful!");
                        // CHỈ THỰC HIỆN CHUYỂN TRANG TỪ ĐÂY
                        navigateToHome(username);
                    } else {
                        AlertUtils.show(AlertType.ERROR, "Authentication Failed", "Invalid username or password.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        AlertUtils.show(AlertType.ERROR, "Connection Error", "Could not connect to the server.");
                    });
                    return null;
                });
    }

    private void navigateToHome(String username) {
        try {
            System.out.println("1. Bắt đầu chuyển sang MainLayout...");
            // Bước 1: Mở khung MainLayout (có thanh điều hướng)
            MainApplication.showView("MainLayout.fxml", "Neth BookPoint");

            // Bước 2: Ép JavaFX phải đợi MainLayout vẽ xong xuôi rồi mới bơm HomeView vào
            Platform.runLater(() -> {
                try {
                    System.out.println("2. Đã nạp MainLayout, bắt đầu bơm HomeView...");
                    NavigationService.getInstance().navigateTo(PageType.HOME, username);
                    System.out.println("3. Hoàn tất chu trình Đăng nhập!");
                } catch (Exception e) {
                    System.err.println("Lỗi nghiêm trọng khi bơm HomeView: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.err.println("Lỗi khi load vỏ MainLayout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void togglePasswordVisibility() {
        model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get());
    }

    public void navigateToRegister() {
        try {
            MainApplication.showView("RegisterView.fxml", "BookStore - Register");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}