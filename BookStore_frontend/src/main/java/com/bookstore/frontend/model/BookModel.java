package com.bookstore.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookModel {
    private Long id;
    private String title;

    @JsonProperty("publisherName")
    private String publisherName;

    @JsonProperty("sellPrice")
    private Double price;

    private Integer quantity;
    private String description;
    private String imageUrl;

    @JsonProperty("authorNames")
    private List<String> authorNamesList;

    private String authorName;

    @JsonProperty("categoryNames")
    private List<String> categoryNames;

    public List<String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(List<String> categoryNames) { this.categoryNames = categoryNames; }

    public String getFormattedCategories() {
        if (categoryNames == null || categoryNames.isEmpty()) return "General";
        return String.join(", ", categoryNames);
    }
    public String getAuthorName() {
        if (authorName != null) return authorName;
        if (authorNamesList != null && !authorNamesList.isEmpty()) {
            return String.join(", ", authorNamesList);
        }
        return "Unknown Author";
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public BookModel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public Double getPrice() { return price != null ? price : 0.0; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


}