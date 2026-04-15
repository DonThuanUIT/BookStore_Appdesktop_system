package com.bookstore.frontend.model;

public class BookDTO {
    private String id;
    private String title;
    private String author;
    private String price;
    private String imageUrl;

    public BookDTO(String id, String title, String author, String price, String imageUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}
