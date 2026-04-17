package com.bookstore.frontend.interactor;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.model.LoginModel;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;
import com.bookstore.frontend.utils.AlertUtils;
import com.bookstore.frontend.util.UserSession; // Import Két sắt
import com.fasterxml.jackson.databind.JsonNode; // Import thư viện parse JSON
import com.fasterxml.jackson.databind.ObjectMapper; // Import thư viện parse JSON
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginInteractor {
    private final LoginModel model;
    private final HttpClient client = HttpClient.newBuilder().build();
    private final ObjectMapper mapper = new ObjectMapper(); // Công cụ đọc JSON

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

        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", user, pass);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200) {
                        // --- ĐOẠN CODE KHÔI PHỤC LƯU TOKEN ---
                        try {
                            // 1. Đọc JSON trả về từ Backend
                            JsonNode jsonNode = mapper.readTree(res.body());
                            String token = jsonNode.get("token").asText();

                            // 2. Cất Token vào két sắt dùng chung
                            UserSession.getInstance().init(token, user);
                            System.out.println("Đã khôi phục và lưu Token tự động cho user: " + user);

                            // 3. Chuyển trang
                            navigateToHome(user);
                        } catch (Exception e) {
                            e.printStackTrace();
                            AlertUtils.show(AlertType.ERROR, "System Error", "Lỗi khi xử lý dữ liệu đăng nhập.");
                        }
                        // --- KẾT THÚC ĐOẠN KHÔI PHỤC ---
                    } else {
                        String errMsg = "Invalid username or password. Please try again.";
                        model.messageProperty().set(errMsg);
                        AlertUtils.show(AlertType.ERROR, "Login Failed", errMsg);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        String errMsg = "Server connection timed out.";
                        model.messageProperty().set(errMsg);
                        AlertUtils.show(AlertType.WARNING, "Connection Issue", "The system could not reach the server.");
                    });
                    return null;
                });
    }

    private void navigateToHome(String username) {
        try {
            MainApplication.showView("MainLayout.fxml", "Neth BookPoint");
            NavigationService.getInstance().navigateTo(PageType.HOME, username);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void navigateToRegister() {
        try { MainApplication.showView("RegisterView.fxml", "Register - BookStore"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void togglePasswordVisibility() {
        model.passwordVisibleProperty().set(!model.passwordVisibleProperty().get());
    }
}