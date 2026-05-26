package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.ImportDetailModel;
import com.bookstore.frontend.model.ImportManagementModel;
import com.bookstore.frontend.model.ImportModel;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.util.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ImportInteractor {

    private final ImportManagementModel managementModel;

    public ImportInteractor(ImportManagementModel managementModel) {
        this.managementModel = managementModel;
    }

    public void loadImportHistory(int page, int size, String keyword) {
        String endpoint = buildListEndpoint(page, size, keyword);

        ApiClient.getInstance().get(endpoint).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonNode root = ApiClient.getInstance().getMapper().readTree(response.body());
                    JsonNode content = root.get("content");
                    List<ImportModel> importList = new ArrayList<>();

                    if (content != null && content.isArray()) {
                        for (JsonNode node : content) {
                            importList.add(parseImportSummary(node));
                        }
                    }

                    int currentPage = root.has("number") ? root.get("number").asInt() : page;
                    int totalPages = root.has("totalPages") ? root.get("totalPages").asInt() : 0;
                    long totalElements = root.has("totalElements") ? root.get("totalElements").asLong() : importList.size();
                    boolean hasNext = root.has("last") && !root.get("last").asBoolean();
                    boolean hasPrevious = root.has("first") && !root.get("first").asBoolean();

                    int from = totalElements == 0 ? 0 : currentPage * size + 1;
                    int to = totalElements == 0 ? 0 : Math.min((currentPage + 1) * size, (int) totalElements);
                    String paginationInfo = String.format("Hiển thị %d–%d / %d phiếu nhập", from, to, totalElements);

                    Platform.runLater(() -> {
                        managementModel.getImports().setAll(importList);
                        managementModel.setCurrentPage(currentPage);
                        managementModel.setTotalPages(totalPages);
                        managementModel.setPageSize(size);
                        managementModel.setTotalRecords((int) totalElements);
                        managementModel.setHasNext(hasNext);
                        managementModel.setHasPrevious(hasPrevious);
                        managementModel.setPaginationInfo(paginationInfo);
                    });
                } catch (Exception e) {
                    System.err.println("Lỗi khi parse dữ liệu Lịch sử Import: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Lỗi API imports: HTTP " + response.statusCode() + " - " + response.body());
            }
        });
    }

    private String buildListEndpoint(int page, int size, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            String encoded = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
            return String.format("/imports/search?keyword=%s&page=%d&size=%d", encoded, page, size);
        }
        return String.format("/imports?page=%d&size=%d", page, size);
    }

    private ImportModel parseImportSummary(JsonNode node) {
        ImportModel importModel = new ImportModel();
        importModel.setId(node.get("id").asLong());
        importModel.setTotalCost(node.get("totalCost").asDouble());
        importModel.setImportDate(formatImportDate(node));
        return importModel;
    }

    private String formatImportDate(JsonNode node) {
        if (!node.has("importDate") || node.get("importDate").isNull()) {
            return "N/A";
        }
        JsonNode dateNode = node.get("importDate");
        if (dateNode.isArray() && dateNode.size() >= 3) {
            return String.format("%02d/%02d/%04d %02d:%02d",
                    dateNode.get(2).asInt(),
                    dateNode.get(1).asInt(),
                    dateNode.get(0).asInt(),
                    dateNode.size() > 3 ? dateNode.get(3).asInt() : 0,
                    dateNode.size() > 4 ? dateNode.get(4).asInt() : 0);
        }
        String raw = dateNode.asText().replace("T", " ");
        if (raw.indexOf('.') > 0) {
            raw = raw.substring(0, raw.indexOf('.'));
        }
        return raw;
    }

    public CompletableFuture<ImportModel> getImportDetails(Long importId) {
        return ApiClient.getInstance().get("/imports/" + importId).thenApply(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonNode node = ApiClient.getInstance().getMapper().readTree(response.body());
                    ImportModel importModel = parseImportSummary(node);

                    if (node.has("details") && node.get("details").isArray()) {
                        for (JsonNode detailNode : node.get("details")) {
                            ImportDetailModel detail = new ImportDetailModel();
                            detail.setBookId(detailNode.get("bookId").asLong());
                            detail.setBookTitle(detailNode.has("bookTitle") && !detailNode.get("bookTitle").isNull()
                                    ? detailNode.get("bookTitle").asText() : "N/A");
                            detail.setQuantity(detailNode.get("quantity").asInt());
                            detail.setImportPrice(detailNode.get("importPrice").asDouble());
                            importModel.getDetails().add(detail);
                        }
                    }
                    return importModel;
                } catch (Exception e) {
                    System.err.println("Lỗi parse Import Details: " + e.getMessage());
                }
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> createImport(List<ImportDetailModel> cartItems) {
        try {
            ObjectNode requestData = ApiClient.getInstance().getMapper().createObjectNode();
            ArrayNode detailsArray = requestData.putArray("details");

            for (ImportDetailModel item : cartItems) {
                ObjectNode detailNode = ApiClient.getInstance().getMapper().createObjectNode();
                detailNode.put("bookId", item.getBookId());
                detailNode.put("quantity", item.getQuantity());
                detailNode.put("importPrice", item.getImportPrice());
                detailsArray.add(detailNode);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.BASE_URL + "/imports"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(requestData.toString()))
                    .build();

            return HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 201 || response.statusCode() == 200);

        } catch (Exception e) {
            System.err.println("LỖI KHI TẠO PHIẾU NHẬP: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    public CompletableFuture<Boolean> deleteImport(Long importId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.BASE_URL + "/imports/" + importId))
                    .header("Authorization", "Bearer " + UserSession.getInstance().getToken())
                    .DELETE()
                    .build();

            return HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 204 || response.statusCode() == 200);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa phiếu nhập: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
