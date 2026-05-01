package com.bookstore.frontend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // Cực kỳ quan trọng: Bỏ qua các thuộc tính thừa của Backend
public class BookResponseDto {
    private Long id;
    private String title;
    private Integer publishYear;
    private BigDecimal sellPrice;
    private String imageUrl;
    private Boolean isDeleted;
    private Integer quantity;

    // Hứng các Object lồng nhau thay vì hứng String/Long
    private PublisherDto publisher;
    private List<AuthorDto> authors;
    private List<CategoryDto> categories;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public BigDecimal getSellPrice() { return sellPrice; }
    public void setSellPrice(BigDecimal sellPrice) { this.sellPrice = sellPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public PublisherDto getPublisher() { return publisher; }
    public void setPublisher(PublisherDto publisher) { this.publisher = publisher; }

    public List<AuthorDto> getAuthors() { return authors; }
    public void setAuthors(List<AuthorDto> authors) { this.authors = authors; }

    public List<CategoryDto> getCategories() { return categories; }
    public void setCategories(List<CategoryDto> categories) { this.categories = categories; }

    // ================= CLASS CON ĐỂ HỨNG DỮ LIỆU LỒNG NHAU =================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublisherDto {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthorDto {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryDto {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}