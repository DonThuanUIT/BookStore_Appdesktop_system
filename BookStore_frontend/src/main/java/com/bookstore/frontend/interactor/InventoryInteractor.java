package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.dto.BookResponseDto;
import com.bookstore.frontend.service.api.BookApiService;
import com.bookstore.frontend.util.MultipartApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryInteractor {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASE_URL = "http://localhost:8080/api/books";


    private String getAuthToken() {
        String token = com.bookstore.frontend.util.UserSession.getInstance().getToken();
        if (token == null) {
            throw new RuntimeException("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn!");
        }
        return token;
    }

    public List<BookModel> fetchAllBooks() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            List<Map<String, Object>> booksData = mapper.readValue(
                    response.body(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<BookModel> list = new ArrayList<>();
            for (Map<String, Object> b : booksData) {
                BookModel book = new BookModel();
                book.setId(((Number) b.get("id")).longValue());
                book.setTitle((String) b.get("title"));

                List<String> authors = (List<String>) b.get("authorNames");
                book.setAuthorName(authors != null && !authors.isEmpty() ? authors.get(0) : "Unknown");

                book.setPublisherName((String) b.get("publisherName"));
                book.setPrice(b.get("sellPrice") != null ? ((Number) b.get("sellPrice")).doubleValue() : 0.0);
                book.setQuantity(b.get("quantity") != null ? ((Number) b.get("quantity")).intValue() : 0);
                book.setImageUrl((String) b.get("imageUrl"));

                list.add(book);
            }
            return list;
        }
        throw new RuntimeException("Lỗi kéo dữ liệu: " + response.statusCode());
    }

    public void saveBook(BookModel book, File imageFile, boolean isEdit) throws Exception {
        // 1. Chuyển thông tin sách thành dạng Map (để Jackson biến thành JSON)
        // Các key ("title", "sellPrice"...) PHẢI KHỚP với class BookUpsertRequest bên Backend
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", book.getTitle());
        requestData.put("sellPrice", book.getPrice());

        // Ghi chú: Ở form nhập có authorId, publisherId. Bạn cần parse chúng ra số và đẩy vào đây.
        // Tạm thời hardcode để test luồng Multipart trước:
        requestData.put("publisherId", 1);
        requestData.put("authorIds", List.of(1));
        requestData.put("categoryIds", List.of(1));

        String jsonBody = mapper.writeValueAsString(requestData);

        // 2. Xác định phương thức HTTP
        String method = isEdit ? "PUT" : "POST";
        String url = isEdit ? BASE_URL + "/" + book.getId() : BASE_URL;

        // 3. Giao việc cho lớp vũ khí OkHttp đã chế tạo
        String serverResponse = MultipartApiClient.sendMultipart(url, method, jsonBody, imageFile, getAuthToken());
        System.out.println("Kết quả từ Server: " + serverResponse);
    }

    public void deleteBook(Long bookId) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + bookId))
                .header("Authorization", "Bearer " + getAuthToken())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new RuntimeException("Xóa thất bại. Mã lỗi: " + response.statusCode());
        }
    }
}