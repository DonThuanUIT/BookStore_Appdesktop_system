package com.bookstore.frontend.model;

import java.util.ArrayList;
import java.util.List;

public class BookModel {
    private Long id;
    private String title;
    private String publisherName;
    private Long publisherId;
    private Integer publishYear;

    private List<String> authorNames = new ArrayList<>();
    private List<Long> authorIds = new ArrayList<>();

    private List<String> categoryNames = new ArrayList<>();
    private List<Long> categoryIds = new ArrayList<>();

    private Double price;
    private Integer quantity;
    private String description;
    private String imageUrl;

    public BookModel() {}

    public String getFormattedCategories() {
        if (categoryNames == null || categoryNames.isEmpty()) return "General";
        return String.join(", ", categoryNames);
    }

    public String getFormattedAuthors() {
        if (authorNames == null || authorNames.isEmpty()) return "Unknown Author";
        return String.join(", ", authorNames);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price != null ? price : 0.0; }
    public void setPrice(Double price) { this.price = price; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public List<String> getAuthorNames() { return authorNames; }
    public void setAuthorNames(List<String> authorNames) { this.authorNames = authorNames; }

    public List<Long> getAuthorIds() { return authorIds; }
    public void setAuthorIds(List<Long> authorIds) { this.authorIds = authorIds; }

    public List<String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(List<String> categoryNames) { this.categoryNames = categoryNames; }

    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
}