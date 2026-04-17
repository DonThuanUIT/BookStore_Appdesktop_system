package com.bookstore.frontend.interactor;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.dto.BookResponseDto;
import com.bookstore.frontend.service.api.BookApiService;

import java.util.ArrayList;
import java.util.List;

public class InventoryInteractor {

    private final BookApiService apiService;

    public InventoryInteractor() {
        this.apiService = new BookApiService();
    }

    public List<BookModel> fetchAllBooks() {
        List<BookResponseDto> dtoList = apiService.getAllBooks();
        List<BookModel> modelList = new ArrayList<>();

        for (BookResponseDto dto : dtoList) {
            BookModel model = new BookModel();
            model.setId(dto.getId());
            model.setTitle(dto.getTitle());
            model.setPrice(dto.getSellPrice() != null ? dto.getSellPrice().doubleValue() : 0.0);

            // Xử lý tạm thời gán Quantity = 0 (vì Backend chưa trả về field này)
            model.setQuantity(0);

            model.setPublisherName(dto.getPublisherName() != null ? dto.getPublisherName() : "Đang cập nhật");

            // Xử lý danh sách Thể loại (Categories) thành 1 chuỗi cách nhau bằng dấu phẩy
            if (dto.getCategoryNames() != null && !dto.getCategoryNames().isEmpty()) {
                model.setDescription(String.join(", ", dto.getCategoryNames()));
                // Tạm thời mượn cột Description để hiển thị Thể loại cho bảng đỡ trống
            } else {
                model.setDescription("Chưa phân loại");
            }

            // Tạm thời gán Tác giả (vì Backend chưa trả về tác giả trong API này)
            model.setAuthorName("Nhiều tác giả");

            modelList.add(model);
        }

        return modelList;
    }
}