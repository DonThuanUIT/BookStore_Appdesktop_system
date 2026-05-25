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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ImportInteractor {

    private final ImportManagementModel managementModel;

    public ImportInteractor(ImportManagementModel managementModel) {
        this.managementModel = managementModel;
    }

    public void loadImportHistory() {
        ApiClient.getInstance().get("/imports").thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonNode rootArray = ApiClient.getInstance().getMapper().readTree(response.body());
                    List<ImportModel> importList = new ArrayList<>();

                    for (JsonNode node : rootArray) {
                        ImportModel importModel = new ImportModel();
                        importModel.setId(node.get("id").asLong());
                        importModel.setTotalCost(node.get("totalCost").asDouble());

                        String importDateStr = "N/A";
                        if (node.has("importDate") && !node.get("importDate").isNull()) {
                            JsonNode dateNode = node.get("importDate");
                            if (dateNode.isArray() && dateNode.size() >= 3) {
                                // Định dạng: dd/MM/yyyy HH:mm
                                importDateStr = String.format("%02d/%02d/%04d %02d:%02d",
                                        dateNode.get(2).asInt(), // Ngày
                                        dateNode.get(1).asInt(), // Tháng
                                        dateNode.get(0).asInt(), // Năm
                                        dateNode.size() > 3 ? dateNode.get(3).asInt() : 0, // Giờ
                                        dateNode.size() > 4 ? dateNode.get(4).asInt() : 0); // Phút
                            } else {
                                String raw = dateNode.asText();
                                importDateStr = raw.replace("T", " ");
                                if(importDateStr.indexOf('.') > 0) {
                                    importDateStr = importDateStr.substring(0, importDateStr.indexOf('.'));
                                }
                            }
                        }
                        importModel.setImportDate(importDateStr);

                        importList.add(importModel);
                    }

                    Platform.runLater(() -> {
                        managementModel.getImports().setAll(importList);
                        managementModel.setTotalRecords(importList.size());
                        managementModel.setPaginationInfo("Tổng cộng: " + importList.size() + " phiếu nhập");
                    });
                } catch (Exception e) {
                    System.err.println("Lỗi khi parse dữ liệu Lịch sử Import: " + e.getMessage());
                }
            }
        });
    }

    public CompletableFuture<ImportModel> getImportDetails(Long importId) {
        return ApiClient.getInstance().get("/imports/" + importId).thenApply(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonNode node = ApiClient.getInstance().getMapper().readTree(response.body());
                    ImportModel importModel = new ImportModel();
                    importModel.setId(node.get("id").asLong());
                    importModel.setTotalCost(node.get("totalCost").asDouble());

                    String importDateStr = "N/A";
                    if (node.has("importDate") && !node.get("importDate").isNull()) {
                        JsonNode dateNode = node.get("importDate");
                        if (dateNode.isArray() && dateNode.size() >= 3) {
                            importDateStr = String.format("%02d/%02d/%04d %02d:%02d",
                                    dateNode.get(2).asInt(), dateNode.get(1).asInt(), dateNode.get(0).asInt(),
                                    dateNode.size() > 3 ? dateNode.get(3).asInt() : 0,
                                    dateNode.size() > 4 ? dateNode.get(4).asInt() : 0);
                        } else {
                            importDateStr = dateNode.asText().replace("T", " ");
                            if(importDateStr.indexOf('.') > 0) importDateStr = importDateStr.substring(0, importDateStr.indexOf('.'));
                        }
                    }
                    importModel.setImportDate(importDateStr);

                    if (node.has("details") && node.get("details").isArray()) {
                        for (JsonNode detailNode : node.get("details")) {
                            ImportDetailModel detail = new ImportDetailModel();
                            detail.setBookId(detailNode.get("bookId").asLong());
                            detail.setBookTitle(detailNode.has("bookTitle") && !detailNode.get("bookTitle").isNull() ? detailNode.get("bookTitle").asText() : "N/A");
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