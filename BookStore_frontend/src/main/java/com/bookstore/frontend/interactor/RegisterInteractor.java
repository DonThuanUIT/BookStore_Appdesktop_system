package com.bookstore.frontend.interactor;

import com.bookstore.frontend.MainApplication;
import com.bookstore.frontend.model.RegisterModel;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

import java.util.Map;
import java.util.regex.Pattern;

public class RegisterInteractor {
    private final RegisterModel model;
    private Timeline countdownTimeline;
    private int remainingSeconds = 180;

    public RegisterInteractor(RegisterModel model) {
        this.model = model;
    }

    public void requestOtp() {
        if (!validateForOtp()) return;

        model.loadingProperty().set(true);
        model.messageProperty().set("");

        Map<String, String> payload = Map.of(
                "username", model.usernameProperty().get().trim(),
                "email", model.emailProperty().get().trim()
        );

        ApiClient.getInstance().post("/auth/register/request-otp", payload)
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 200) {
                        AlertUtils.show(AlertType.INFORMATION, "Đã gửi OTP", "Mã xác thực đã được gửi tới Email của bạn.");
                        model.otpRequestedProperty().set(true);
                        startCountdown();
                    } else {
                        model.messageProperty().set("Tên đăng nhập hoặc Email đã tồn tại hoặc không hợp lệ.");
                        AlertUtils.show(AlertType.ERROR, "Lỗi yêu cầu OTP", "Chi tiết từ máy chủ:\n" + res.body());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        model.messageProperty().set("Lỗi kết nối máy chủ.");
                    });
                    return null;
                });
    }

    public void register() {
        if (!validateForRegister()) return;

        model.loadingProperty().set(true);
        model.messageProperty().set("");

        Map<String, String> payload = Map.of(
                "username", model.usernameProperty().get().trim(),
                "email", model.emailProperty().get().trim(),
                "password", model.passwordProperty().get(),
                "otpCode", model.otpProperty().get().trim()
        );

        ApiClient.getInstance().post("/auth/register/verify", payload)
                .thenAccept(res -> Platform.runLater(() -> {
                    model.loadingProperty().set(false);
                    if (res.statusCode() == 201) {
                        stopCountdown();
                        AlertUtils.show(AlertType.INFORMATION, "Thành công", "Tài khoản của bạn đã được tạo thành công!");
                        navigateToLogin();
                    } else {
                        // IN RA LỖI THẬT SỰ ĐỂ DEBUG
                        System.out.println("HTTP " + res.statusCode() + " - Lỗi từ Backend: " + res.body());
                        model.messageProperty().set("Dữ liệu không hợp lệ. Hãy kiểm tra lại định dạng Mật khẩu hoặc mã OTP.");
                        AlertUtils.show(AlertType.ERROR, "Đăng ký thất bại", "Chi tiết lỗi từ máy chủ:\n" + res.body());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        model.loadingProperty().set(false);
                        model.messageProperty().set("Lỗi kết nối máy chủ.");
                    });
                    return null;
                });
    }

    private void startCountdown() {
        remainingSeconds = 180;
        if (countdownTimeline != null) countdownTimeline.stop();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            int m = remainingSeconds / 60;
            int s = remainingSeconds % 60;
            model.countdownTextProperty().set(String.format("%02d:%02d", m, s));

            if (remainingSeconds <= 0) {
                stopCountdown();
                model.messageProperty().set("Mã OTP đã hết hạn, vui lòng yêu cầu gửi lại.");
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) countdownTimeline.stop();
        model.otpRequestedProperty().set(false);
        model.countdownTextProperty().set("03:00");
    }

    private boolean validateForOtp() {
        String username = model.usernameProperty().get().trim();
        String email = model.emailProperty().get().trim();

        if (username.isBlank() || email.isBlank()) {
            AlertUtils.show(AlertType.WARNING, "Cảnh báo", "Username và Email không được để trống!");
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.compile(emailRegex).matcher(email).matches()) {
            AlertUtils.show(AlertType.WARNING, "Cảnh báo", "Định dạng Email không hợp lệ!");
            return false;
        }
        return true;
    }

    private boolean validateForRegister() {
        if (!validateForOtp()) return false;

        if (model.otpProperty().get().isBlank()) {
            AlertUtils.show(AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mã OTP!");
            return false;
        }
        if (model.passwordProperty().get().isBlank()) {
            AlertUtils.show(AlertType.WARNING, "Cảnh báo", "Mật khẩu không được để trống!");
            return false;
        }
        if (!model.passwordProperty().get().equals(model.confirmPasswordProperty().get())) {
            AlertUtils.show(AlertType.WARNING, "Cảnh báo", "Mật khẩu nhập lại không khớp!");
            return false;
        }
        return true;
    }

    public void navigateToLogin() {
        try {
            stopCountdown();
            MainApplication.showView("LoginView.fxml", "Login - BookStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}