package com.bookstore.frontend.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ImportManagementModel {
    // Danh sách phiếu nhập hiển thị trên TableView
    private final ObservableList<ImportModel> imports = FXCollections.observableArrayList();

    // Binding cho phân trang (Pagination)
    private final IntegerProperty totalRecords = new SimpleIntegerProperty(0);
    private final StringProperty paginationInfo = new SimpleStringProperty("Showing 0 entries");

    public ObservableList<ImportModel> getImports() { return imports; }

    public IntegerProperty totalRecordsProperty() { return totalRecords; }
    public void setTotalRecords(int total) { this.totalRecords.set(total); }

    public StringProperty paginationInfoProperty() { return paginationInfo; }
    public void setPaginationInfo(String info) { this.paginationInfo.set(info); }
}