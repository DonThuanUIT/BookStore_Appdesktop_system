package com.bookstore.frontend.controller;

import com.bookstore.frontend.model.dto.Request.UpdateProfileRequest;
import com.bookstore.frontend.model.dto.UserProfileDTO;
import com.bookstore.frontend.service.api.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProfileController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtRoles; // Thêm trường mới

    public void initialize() {
        System.out.println("ProfileController đã khởi tạo.");
        loadUserProfile();
    }

    private void loadUserProfile() {
        ApiClient.getInstance().get("/users/profile").thenAccept(response -> {
            System.out.println("Status Code nhận được: " + response.statusCode()); // Kiểm tra log
            if (response.statusCode() == 200) {
                try {
                    UserProfileDTO user = ApiClient.getInstance().getMapper().readValue(response.body(), UserProfileDTO.class);
                    Platform.runLater(() -> {
                        txtUsername.setText(user.username);
                        txtFullName.setText(user.fullName);
                        txtEmail.setText(user.email);
                    });
                } catch (Exception e) {
                    e.printStackTrace(); // Xem kỹ lỗi ở console
                }
            } else {
                System.out.println("API trả về lỗi: " + response.body());
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    private void handleUpdate() {
        // Chỉ tạo object với các thông tin cho phép sửa
        UpdateProfileRequest request = new UpdateProfileRequest(txtEmail.getText(), txtFullName.getText());

        ApiClient.getInstance().put("/users/profile", request).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> System.out.println("Cập nhật thành công!"));
            } else {
                // In ra body lỗi để xem tại sao 403
                System.err.println("Lỗi 403/400: " + response.body());
            }
        });
    }
}