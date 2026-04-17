package com.bookstore.frontend.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO thống nhất để nhận dữ liệu từ API /api/books.
 * Phục vụ cho cả màn hình Quản lý kho (Inventory) và Cửa hàng (Shop).
 */
public class BookResponseDto {
    // --- Thông tin cơ bản (Dùng chung) ---
    private Long id;
    private String title;
    private BigDecimal sellPrice;
    private String imageUrl;

    // --- Thông tin Tác giả & NXB (Dùng chung) ---
    private List<String> authorNames;
    private List<Long> authorIds; // Cần khi thực hiện chức năng Edit ở Inventory
    private String publisherName;
    private Long publisherId;

    // --- Thông tin Quản lý (Inventory ưu tiên) ---
    private Integer quantity; // Rất quan trọng cho Inventory [cite: 3]
    private Integer publishYear;
    private Boolean isDeleted;

    // --- Thông tin chi tiết (Shop Detail ưu tiên) ---
    private String description; // Dùng để hiện nội dung khi nhấn vào Card sách
    private List<String> categoryNames;
    private List<Long> categoryIds;

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getSellPrice() { return sellPrice; }
    public void setSellPrice(BigDecimal sellPrice) { this.sellPrice = sellPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getAuthorNames() { return authorNames; }
    public void setAuthorNames(List<String> authorNames) { this.authorNames = authorNames; }

    public List<Long> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<Long> authorIds) { this.authorIds = authorIds; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(List<String> categoryNames) { this.categoryNames = categoryNames; }

    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
}