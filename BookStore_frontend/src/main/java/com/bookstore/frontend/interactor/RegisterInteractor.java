package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.RegisterModel;

public class RegisterInteractor {
    private final RegisterModel model;

    public RegisterInteractor(RegisterModel model) {
        this.model = model;
    }

    public void register() {
        String fullName = model.getFullName();
        String email = model.getEmail();
        String password = model.getPassword();
        String confirmPassword = model.getConfirmPassword();

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            model.setMessage("Please fill in all fields.");
            return;
        }
        if (!email.contains("@")) {
            model.setMessage("Invalid email.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            model.setMessage("Passwords do not match.");
            return;
        }

        model.setMessage("Register successful!");
    }
}