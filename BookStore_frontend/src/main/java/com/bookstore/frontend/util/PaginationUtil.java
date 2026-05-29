package com.bookstore.frontend.util;

/**
 * Utility class cho logic phân trang
 * Unify cách tính toán phân trang giữa các view khác nhau
 */
public class PaginationUtil {
    
    /**
     * Tính toán tổng số trang
     */
    public static int calculateTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0) return 1;
        return (int) Math.ceil((double) totalItems / pageSize);
    }
    
    /**
     * Lấy số item từ
     */
    public static int getFromIndex(int currentPage, int pageSize) {
        return Math.min(currentPage * pageSize, Integer.MAX_VALUE);
    }
    
    /**
     * Lấy số item đến
     */
    public static int getToIndex(int currentPage, int pageSize, int totalItems) {
        return Math.min((currentPage + 1) * pageSize, totalItems);
    }
    
    /**
     * Kiểm tra có trang tiếp theo không
     */
    public static boolean hasNextPage(int currentPage, int totalPages) {
        return currentPage < totalPages - 1 && totalPages > 0;
    }
    
    /**
     * Kiểm tra có trang trước không
     */
    public static boolean hasPreviousPage(int currentPage) {
        return currentPage > 0;
    }
    
    /**
     * Format pagination info
     * @param currentPage 0-based
     * @param pageSize
     * @param totalItems
     */
    public static String formatPaginationInfo(int currentPage, int pageSize, int totalItems) {
        if (totalItems == 0) return "Không có dữ liệu";
        
        int from = getFromIndex(currentPage, pageSize) + 1;
        int to = getToIndex(currentPage, pageSize, totalItems);
        int totalPages = calculateTotalPages(totalItems, pageSize);
        
        return String.format("Trang %d/%d (%d-%d của %d)",
                currentPage + 1,
                totalPages,
                from,
                to,
                totalItems);
    }
    
    /**
     * Format pagination info cho Shop view
     */
    public static String formatShopPaginationInfo(int currentPage, int pageSize, int totalItems) {
        if (totalItems == 0) return "Không tìm thấy sách";
        
        int totalPages = calculateTotalPages(totalItems, pageSize);
        return String.format("Trang %d/%d",
                currentPage + 1,
                Math.max(1, totalPages));
    }
    
    /**
     * Clamp current page để không vượt quá max
     */
    public static int clampPage(int currentPage, int totalPages) {
        if (totalPages == 0) return 0;
        return Math.max(0, Math.min(currentPage, totalPages - 1));
    }
}
