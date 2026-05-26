package com.bookstore.frontend.service.api;

import com.bookstore.frontend.util.UserSession;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public ObjectMapper getMapper() {
        return objectMapper;
    }

    // =========================================================================
    // HÀM GỌI API (GET, POST, PUT, PATCH, UPLOAD)
    // =========================================================================

    public CompletableFuture<HttpResponse<String>> get(String endpoint) {
        return sendRequest(HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET());
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint, Object body) {
        try {
            return sendRequest(HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> post(String endpoint) {
        return sendRequest(HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .POST(HttpRequest.BodyPublishers.noBody()));
    }

    public CompletableFuture<HttpResponse<String>> put(String endpoint, Object body) {
        try {
            return sendRequest(HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> patch(String endpoint, Object body) {
        try {
            return sendRequest(HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> uploadFile(String endpoint, File file) {
        try {
            String boundary = "----JavaFxFormBoundary" + System.currentTimeMillis();
            byte[] bodyData = buildMultipartBody(file, boundary);

            return sendRequest(HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyData)));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // =========================================================================
    // HÀM HỖ TRỢ (HELPERS)
    // =========================================================================

    private CompletableFuture<HttpResponse<String>> sendRequest(HttpRequest.Builder builder) {
        attachAuthToken(builder);
        return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void attachAuthToken(HttpRequest.Builder requestBuilder) {
        String token = UserSession.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
    }

    private byte[] buildMultipartBody(File file, String boundary) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) mimeType = "application/octet-stream";

        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                "Content-Type: " + mimeType + "\r\n\r\n";
        String footer = "\r\n--" + boundary + "--\r\n";

        ByteBuffer body = ByteBuffer.allocate(header.length() + fileBytes.length + footer.length());
        body.put(header.getBytes(StandardCharsets.UTF_8));
        body.put(fileBytes);
        body.put(footer.getBytes(StandardCharsets.UTF_8));

        return body.array();
    }
}