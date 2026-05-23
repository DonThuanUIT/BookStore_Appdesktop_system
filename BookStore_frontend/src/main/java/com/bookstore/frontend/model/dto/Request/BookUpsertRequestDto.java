package com.bookstore.frontend.model.dto.Request;

import java.util.List;

public class BookUpsertRequestDto {
    private String title;
    private String description;
    private String imageUrl;
    private Double sellPrice;
    private Long publisherId;
    private List<Long> authorIds;
    private List<Long> categoryIds;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getSellPrice() { return sellPrice; }
    public void setSellPrice(Double sellPrice) { this.sellPrice = sellPrice; }

    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }

    public List<Long> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<Long> authorIds) { this.authorIds = authorIds; }

    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
}
