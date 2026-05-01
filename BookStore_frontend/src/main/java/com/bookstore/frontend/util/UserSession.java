package com.bookstore.frontend.util;

public class UserSession {
    private static UserSession instance;
    private String token;
    private String username;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Lưu Token vào đây sau khi login thành công
    public void init(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }

    public void clean() {
        token = null;
        username = null;
    }
}