package com.bookstore.frontend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Lớp DTO Generic dùng để hứng dữ liệu phân trang (Page<T>) từ Spring Boot Backend.
 * Chữ <T> đại diện cho bất kỳ kiểu DTO nào (Ví dụ: BookResponseDto, OrderDto...)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponseDto<T> {

    // Chứa danh sách dữ liệu thực tế (Ví dụ: Danh sách 12 cuốn sách)
    private List<T> content;

    // Tổng số trang có trong Database
    private int totalPages;

    // Tổng số phần tử có trong Database
    private long totalElements;

    // Trang hiện tại (Bắt đầu từ 0)
    private int number;

    // Kích thước của 1 trang (Ví dụ: 12)
    private int size;

    // Cờ đánh dấu đây có phải là trang cuối cùng chưa? (Rất quan trọng cho Lazy Loading)
    private boolean last;

    // Cờ đánh dấu đây có phải là trang đầu tiên không?
    private boolean first;

    // Cờ đánh dấu danh sách có trống không?
    private boolean empty;

    // --- Getters & Setters ---

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
}