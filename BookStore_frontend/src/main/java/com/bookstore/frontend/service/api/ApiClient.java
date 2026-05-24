package com.bookstore.frontend.service.api;

import com.bookstore.frontend.util.UserSession;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Thêm import này

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static ApiClient instance;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    public static final String BASE_URL = "http://localhost:8080/api";

    private ApiClient() {
        // Khởi tạo HttpClient dùng chung, set timeout 10s để tránh treo app
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();

        // 1. Đăng ký module để xử lý LocalDateTime
        this.objectMapper.registerModule(new JavaTimeModule());

        // 2. Cấu hình để bỏ qua các trường lạ (như 'discount')
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public ObjectMapper getMapper() {
        return objectMapper;
    }

    // =========================================================================
    // CÁC HÀM GỌI API CHÍNH (GET, POST, UPLOAD)
    // =========================================================================

    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .GET();

            attachAuthToken(requestBuilder);

            return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            attachAuthToken(requestBuilder);

            return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> put(String endpoint, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

            attachAuthToken(requestBuilder);

            return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> uploadFile(String endpoint, File file) throws IOException {
        String boundary = "----JavaFxFormBoundary" + System.currentTimeMillis();
        // Đẩy logic xử lý byte phức tạp xuống hàm helper để class sạch sẽ hơn
        byte[] bodyData = buildMultipartBody(file, boundary);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyData));

        attachAuthToken(requestBuilder);

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    // =========================================================================
    // HÀM PHỤ TRỢ (HELPERS) - Tách biệt logic để code dễ bảo trì
    // =========================================================================

    /**
     * Tự động lấy token từ Két sắt (UserSession) và gắn vào Header
     */
    private void attachAuthToken(HttpRequest.Builder requestBuilder) {
        String token = UserSession.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
    }

    /**
     * Đóng gói file thành khối byte theo chuẩn multipart/form-data
     */
    private byte[] buildMultipartBody(File file, String boundary) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("--").append(boundary).append("\r\n");
        headerBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
        headerBuilder.append("Content-Type: ").append(mimeType).append("\r\n\r\n");
        byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);

        byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        ByteBuffer body = ByteBuffer.allocate(headerBytes.length + fileBytes.length + footerBytes.length);
        body.put(headerBytes);
        body.put(fileBytes);
        body.put(footerBytes);

        return body.array();
    }

    // Thêm vào class ApiClient
    public CompletableFuture<HttpResponse<String>> patch(String endpoint, String jsonBody) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody));

            attachAuthToken(requestBuilder);

            return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
