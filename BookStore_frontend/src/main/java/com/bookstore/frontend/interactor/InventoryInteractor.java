package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import java.util.ArrayList;
import java.util.List;

public class InventoryInteractor {

    // Hàm tạo dữ liệu giả để test UI
    public List<BookModel> fetchMockBooks() {
        List<BookModel> mockList = new ArrayList<>();

        BookModel book1 = new BookModel();
        book1.setId(1L);
        book1.setTitle("Sword Art Online Progressive Vol 7");
        book1.setAuthorName("REKI KAWAHARA");
        book1.setPublisherName("IPM, Hà Nội");
        book1.setPrice(120000.0);
        book1.setQuantity(50);
        book1.setDescription("Dễ thấy Sword Art Online có không gian kể chuyện rất rộng...");

        BookModel book2 = new BookModel();
        book2.setId(2L);
        book2.setTitle("Sword Art Online Progressive Vol 8");
        book2.setAuthorName("REKI KAWAHARA");
        book2.setPublisherName("IPM, Hà Nội");
        book2.setPrice(125000.0);
        book2.setQuantity(30);
        book2.setDescription("Tiếp nối câu chuyện ở tầng 7 của Aincrad...");

        BookModel book3 = new BookModel();
        book3.setId(3L);
        book3.setTitle("Đắc Nhân Tâm");
        book3.setAuthorName("Dale Carnegie");
        book3.setPublisherName("First News");
        book3.setPrice(85000.0);
        book3.setQuantity(100);
        book3.setDescription("Nghệ thuật thu phục lòng người...");

        mockList.add(book1);
        mockList.add(book2);
        mockList.add(book3);

        return mockList;
    }
}