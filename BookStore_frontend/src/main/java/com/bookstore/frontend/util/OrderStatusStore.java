package com.bookstore.frontend.util;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class OrderStatusStore {
    private static OrderStatusStore instance;
    private final IntegerProperty pendingCount = new SimpleIntegerProperty(0);

    public static OrderStatusStore getInstance() {
        if (instance == null) instance = new OrderStatusStore();
        return instance;
    }

    public IntegerProperty pendingCountProperty() { return pendingCount; }

    public int getPendingCount() { return pendingCount.get(); }

    // Gọi hàm này sau khi thanh toán thành công
    public void incrementPendingOrder() {
        pendingCount.set(pendingCount.get() + 1);
    }

    // Gọi hàm này khi admin duyệt đơn (để giảm số lượng)
    public void decrementPendingOrder() {
        if (pendingCount.get() > 0) {
            pendingCount.set(pendingCount.get() - 1);
        }
    }
}