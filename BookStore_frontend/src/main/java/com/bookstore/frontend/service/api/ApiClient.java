package com.bookstore.frontend.service.api;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.dto.Response.BookResponseDto;
import com.bookstore.frontend.util.BookMapper;
import com.bookstore.frontend.util.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ApiClient {
    private static ApiClient instance;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    public static final String BASE_URL = "http://localhost:8080/api";

    private final List<Consumer<BookModel>> bookUpdateListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Long>> bookDeleteListeners = new CopyOnWriteArrayList<>();

    private final List<Consumer<com.bookstore.frontend.model.ImportModel>> importCreateListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Long>> importDeleteListeners = new CopyOnWriteArrayList<>();

    public void onImportCreated(Consumer<com.bookstore.frontend.model.ImportModel> listener) {
        importCreateListeners.add(listener);
    }
    public void onImportDeleted(Consumer<Long> listener) {
        importDeleteListeners.add(listener);
    }

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
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

    // Thêm phương thức này để hỗ trợ các request POST không body
    public CompletableFuture<HttpResponse<String>> post(String endpoint) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .POST(HttpRequest.BodyPublishers.noBody()); // Không có body

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
        byte[] bodyData = buildMultipartBody(file, boundary);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyData));

        attachAuthToken(requestBuilder);

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public void onBookUpdated(Consumer<BookModel> listener) {
        bookUpdateListeners.add(listener);
    }

    public void onBookDeleted(Consumer<Long> listener) {
        bookDeleteListeners.add(listener);
    }


    public void startSseConnection() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/notifications/stream"))
                .header("Accept", "text/event-stream")
                .GET();
        attachAuthToken(requestBuilder);

        httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    final String[] currentEvent = {"message"};

                    response.body().forEach(line -> {
                        if (line.startsWith("event:")) {
                            currentEvent[0] = line.substring(6).trim();
                        } else if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            processSseEvent(currentEvent[0], data);
                        }
                    });
                })
                .exceptionally(e -> {
                    System.err.println("SSE Connection Lost or Error: " + e.getMessage());
                    CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                            .execute(this::startSseConnection);
                    return null;
                });
    }

    private void processSseEvent(String eventName, String data) {
        try {
            if ("UPDATE_BOOK".equals(eventName)) {
                BookResponseDto dto = objectMapper.readValue(data, BookResponseDto.class);
                BookModel updatedBook = BookMapper.toModel(dto);

                bookUpdateListeners.forEach(listener -> listener.accept(updatedBook));

            } else if ("DELETE_BOOK".equals(eventName)) {
                Long bookId = Long.parseLong(data);

                bookDeleteListeners.forEach(listener -> listener.accept(bookId));
            } else if ("CREATE_IMPORT".equals(eventName)) {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(data);
                com.bookstore.frontend.model.ImportModel importModel = new com.bookstore.frontend.model.ImportModel();
                importModel.setId(node.get("id").asLong());
                if (node.has("adminUsername") && !node.get("adminUsername").isNull()) {
                    importModel.setAdminUsername(node.get("adminUsername").asText());
                }
                importModel.setTotalCost(node.get("totalCost").asDouble());

                String importDateStr = "N/A";
                if (node.has("importDate") && !node.get("importDate").isNull()) {
                    com.fasterxml.jackson.databind.JsonNode dateNode = node.get("importDate");
                    if (dateNode.isArray() && dateNode.size() >= 3) {
                        importDateStr = String.format("%02d/%02d/%04d %02d:%02d",
                                dateNode.get(2).asInt(), dateNode.get(1).asInt(), dateNode.get(0).asInt(),
                                dateNode.size() > 3 ? dateNode.get(3).asInt() : 0,
                                dateNode.size() > 4 ? dateNode.get(4).asInt() : 0);
                    } else {
                        String raw = dateNode.asText();
                        importDateStr = raw.replace("T", " ");
                        if(importDateStr.indexOf('.') > 0) importDateStr = importDateStr.substring(0, importDateStr.indexOf('.'));
                    }
                }
                importModel.setImportDate(importDateStr);

                importCreateListeners.forEach(listener -> listener.accept(importModel));

            } else if ("DELETE_IMPORT".equals(eventName)) {
                Long importId = Long.parseLong(data);
                importDeleteListeners.forEach(listener -> listener.accept(importId));
            }
        } catch (Exception e) {
            System.err.println("Error parsing SSE data: " + e.getMessage());
        }
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
