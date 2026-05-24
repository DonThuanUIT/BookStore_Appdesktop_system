package com.bookstore.frontend.controller;

import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.model.dto.Request.UserProfileUpdateRequest;
import com.bookstore.frontend.model.dto.Response.UserProfileResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProfileController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        txtUsername.setEditable(false);
        loadUserProfile();
    }

    private void loadUserProfile() {
        ApiClient.getInstance().get("/auth/me").thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    UserProfileResponseDto profile = objectMapper.readValue(response.body(), UserProfileResponseDto.class);
                    Platform.runLater(() -> {
                        txtUsername.setText(profile.username());
                        txtFullName.setText(profile.fullName());
                        txtEmail.setText(profile.email());
                    });
                } catch (JsonProcessingException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi xử lý dữ liệu hồ sơ!");
                        alert.show();
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleUpdate() {
        // Lưu ý: Nếu FXML có thêm phone/address, hãy thay null bằng giá trị từ TextField
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                txtEmail.getText(),
                txtFullName.getText(),
                null,
                null
        );

        ApiClient.getInstance().put("/auth/me", request).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cập nhật thành công!");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại: " + response.body());
                    alert.show();
                }
            });
        });
    }
}