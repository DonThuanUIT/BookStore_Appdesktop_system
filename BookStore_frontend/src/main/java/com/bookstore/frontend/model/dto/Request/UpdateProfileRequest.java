package com.bookstore.frontend.model.dto.Request;

public class UpdateProfileRequest {
    public String email;
    public String fullName;
    public String phone;
    public String address;

    public UpdateProfileRequest(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }
}