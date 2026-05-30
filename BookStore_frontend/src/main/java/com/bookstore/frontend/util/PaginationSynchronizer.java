package com.bookstore.frontend.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Đồng bộ hóa phân trang giữa 3 trang: Shop, Inventory, ImportHistory
 * Khi một trang thay đổi trang, tất cả sẽ cập nhật cùng lúc
 */
public class PaginationSynchronizer {
    
    private static final PaginationSynchronizer instance = new PaginationSynchronizer();
    private final List<PaginationListener> listeners = new ArrayList<>();
    
    // Trạng thái hiện tại của mỗi trang
    private int currentShopPage = 0;
    private int currentInventoryPage = 0;
    private int currentImportHistoryPage = 0;
    
    private boolean syncEnabled = true;
    
    private PaginationSynchronizer() {}
    
    public static PaginationSynchronizer getInstance() {
        return instance;
    }
    
    /**
     * Interface để lắng nghe sự thay đổi phân trang
     */
    public interface PaginationListener {
        void onPageChanged(String pageType, int page, int pageSize);
    }
    
    /**
     * Đăng ký listener để theo dõi sự thay đổi phân trang
     */
    public void addListener(PaginationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Xóa listener
     */
    public void removeListener(PaginationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Thay đổi trang Shop
     */
    public void setShopPage(int page, int pageSize) {
        if (this.currentShopPage != page) {
            this.currentShopPage = page;
            notifyListeners("SHOP", page, pageSize);
        }
    }
    
    /**
     * Thay đổi trang Inventory
     */
    public void setInventoryPage(int page, int pageSize) {
        if (this.currentInventoryPage != page) {
            this.currentInventoryPage = page;
            notifyListeners("INVENTORY", page, pageSize);
        }
    }
    
    /**
     * Thay đổi trang ImportHistory
     */
    public void setImportHistoryPage(int page, int pageSize) {
        if (this.currentImportHistoryPage != page) {
            this.currentImportHistoryPage = page;
            notifyListeners("IMPORT_HISTORY", page, pageSize);
        }
    }
    
    /**
     * Lấy trang hiện tại của Shop
     */
    public int getShopPage() {
        return currentShopPage;
    }
    
    /**
     * Lấy trang hiện tại của Inventory
     */
    public int getInventoryPage() {
        return currentInventoryPage;
    }
    
    /**
     * Lấy trang hiện tại của ImportHistory
     */
    public int getImportHistoryPage() {
        return currentImportHistoryPage;
    }
    
    /**
     * Thông báo cho tất cả listeners về sự thay đổi trang
     */
    private void notifyListeners(String pageType, int page, int pageSize) {
        if (syncEnabled) {
            for (PaginationListener listener : listeners) {
                listener.onPageChanged(pageType, page, pageSize);
            }
        }
    }
    
    /**
     * Bật/tắt đồng bộ hóa
     */
    public void setSyncEnabled(boolean enabled) {
        this.syncEnabled = enabled;
    }
    
    /**
     * Kiểm tra đồng bộ hóa có bật không
     */
    public boolean isSyncEnabled() {
        return syncEnabled;
    }
    
    /**
     * Reset tất cả trang về 0
     */
    public void resetAllPages() {
        this.currentShopPage = 0;
        this.currentInventoryPage = 0;
        this.currentImportHistoryPage = 0;
    }
    
    /**
     * Reset một trang cụ thể
     */
    public void resetPage(String pageType) {
        switch(pageType) {
            case "SHOP":
                this.currentShopPage = 0;
                break;
            case "INVENTORY":
                this.currentInventoryPage = 0;
                break;
            case "IMPORT_HISTORY":
                this.currentImportHistoryPage = 0;
                break;
        }
    }
}
