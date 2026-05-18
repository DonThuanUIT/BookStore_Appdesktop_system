package com.bookstore.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "Username không được để trống")
        @Size(max = 50, message = "Username tối đa 50 ký tự")
        String username,

        @NotBlank(message = "Password không được để trống")
        @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
        String password,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 100, message = "Email tối đa 100 ký tự")
        String email,

        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String fullName,

        @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
        String phone,

        String address,

        @NotBlank(message = "Role không được để trống")
        String roleName
) {
}
