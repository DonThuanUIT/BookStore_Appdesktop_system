package com.bookstore.frontend.util;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class MultipartApiClient {
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Gửi request có chứa cả JSON Data và File ảnh lên Backend
     * @param url Đường dẫn API (VD: http://localhost:8080/api/books)
     * @param method Phương thức HTTP ("POST" để Thêm, "PUT" để Sửa)
     * @param jsonBody Chuỗi JSON chứa thông tin sách (title, price...)
     * @param imageFile File ảnh người dùng chọn từ máy tính (có thể null nếu không cập nhật ảnh)
     * @param jwtToken Mã Token để xác thực người dùng (Bắt buộc vì API yêu cầu quyền ADMIN/STAFF)
     * @return Chuỗi JSON trả về từ Server
     */
    public static String sendMultipart(String url, String method, String jsonBody, File imageFile, String jwtToken) throws IOException {

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            bodyBuilder.addFormDataPart("data", null,
                    RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")));
        }

        if (imageFile != null && imageFile.exists()) {
            bodyBuilder.addFormDataPart("image", imageFile.getName(),
                    RequestBody.create(imageFile, MediaType.parse("image/*")));
        }

        RequestBody requestBody = bodyBuilder.build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + jwtToken); // Truyền Token vào Header

        if ("POST".equalsIgnoreCase(method)) {
            requestBuilder.post(requestBody);
        } else if ("PUT".equalsIgnoreCase(method)) {
            requestBuilder.put(requestBody);
        } else {
            throw new IllegalArgumentException("Chỉ hỗ trợ phương thức POST hoặc PUT cho Multipart");
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Lỗi từ Server: " + response.code() + " - " + response.message());
            }
            return response.body() != null ? response.body().string() : "";
        }
    }
}