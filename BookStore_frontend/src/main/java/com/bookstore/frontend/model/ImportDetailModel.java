package com.bookstore.frontend.model;

import javafx.beans.property.*;

public class ImportDetailModel {
    private final LongProperty bookId = new SimpleLongProperty();
    private final StringProperty bookTitle = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty(1);
    private final DoubleProperty importPrice = new SimpleDoubleProperty(0.0);
    private final DoubleProperty lineTotal = new SimpleDoubleProperty(0.0);

    // --- GETTERS / SETTERS / PROPERTIES ---
    public long getBookId() { return bookId.get(); }
    public void setBookId(long value) { bookId.set(value); }
    public LongProperty bookIdProperty() { return bookId; }

    public String getBookTitle() { return bookTitle.get(); }
    public void setBookTitle(String value) { bookTitle.set(value); }
    public StringProperty bookTitleProperty() { return bookTitle; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) {
        quantity.set(value);
        updateLineTotal();
    }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getImportPrice() { return importPrice.get(); }
    public void setImportPrice(double value) {
        importPrice.set(value);
        updateLineTotal();
    }
    public DoubleProperty importPriceProperty() { return importPrice; }

    public double getLineTotal() { return lineTotal.get(); }
    public DoubleProperty lineTotalProperty() { return lineTotal; }

    // Tự động cập nhật thành tiền mỗi khi số lượng hoặc giá thay đổi
    private void updateLineTotal() {
        this.lineTotal.set(getQuantity() * getImportPrice());
    }
}
