package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationVerifyRequest(
        @NotBlank(message = "Username không được để trống")
        String username,

        @NotBlank(message = "Password không được để trống")
        @Size(min = 6, max = 255, message = "Password phải từ 6 ký tự trở lên")
        String password,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Định dạng Email không hợp lệ")
        String email,

        @NotBlank(message = "Mã OTP không được để trống")
        @Pattern(regexp = "^[0-9]{6}$", message = "Mã OTP phải là chuỗi 6 chữ số")
        String otpCode
) {
}