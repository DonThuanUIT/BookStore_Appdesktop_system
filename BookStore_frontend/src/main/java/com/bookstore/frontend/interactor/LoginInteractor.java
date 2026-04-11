package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.LoginModel;

public class LoginInteractor {
    private final LoginModel model;

    public LoginInteractor(LoginModel model) {
        this.model = model;
    }

    // Xử lý logic đăng nhập
    public void login() {
        String user = model.usernameProperty().get();
        String pass = model.passwordProperty().get();

        if ("admin".equals(user) && "123".equals(pass)) {
            model.messageProperty().set("Login successfully!");
        } else {
            model.messageProperty().set("Wrong username or password!");
        }
    }

    // Logic chuyển đổi trạng thái ẩn/hiện mật khẩu
    public void togglePasswordVisibility() {
        boolean isCurrentlyVisible = model.passwordVisibleProperty().get();
        model.passwordVisibleProperty().set(!isCurrentlyVisible);
    }
}