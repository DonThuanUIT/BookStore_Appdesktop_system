package com.bookstore.frontend.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryModel {
    private final ObservableList<BookModel> books = FXCollections.observableArrayList();
    private final IntegerProperty totalTitles = new SimpleIntegerProperty(0);
    private final IntegerProperty lowStockCount = new SimpleIntegerProperty(0);
    private final StringProperty paginationInfo = new SimpleStringProperty("Showing 0 entries");

    // Getters cho các Property để Controller thực hiện Binding
    public ObservableList<BookModel> getBooks() { return books; }
    public IntegerProperty totalTitlesProperty() { return totalTitles; }
    public IntegerProperty lowStockCountProperty() { return lowStockCount; }
    public StringProperty paginationInfoProperty() { return paginationInfo; }
    public void setPaginationInfo(String info) {
        this.paginationInfo.set(info);
    }
}