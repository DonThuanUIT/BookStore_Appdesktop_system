package com.bookstore.frontend.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ImportManagementModel {
    private final ObservableList<ImportModel> imports = FXCollections.observableArrayList();

    private final IntegerProperty currentPage = new SimpleIntegerProperty(0);
    private final IntegerProperty totalPages = new SimpleIntegerProperty(0);
    private final IntegerProperty pageSize = new SimpleIntegerProperty(10);
    private final IntegerProperty totalRecords = new SimpleIntegerProperty(0);
    private final StringProperty paginationInfo = new SimpleStringProperty("Showing 0 entries");
    private final BooleanProperty hasNext = new SimpleBooleanProperty(false);
    private final BooleanProperty hasPrevious = new SimpleBooleanProperty(false);

    public ObservableList<ImportModel> getImports() { return imports; }

    public IntegerProperty currentPageProperty() { return currentPage; }
    public int getCurrentPage() { return currentPage.get(); }
    public void setCurrentPage(int page) { this.currentPage.set(page); }

    public IntegerProperty totalPagesProperty() { return totalPages; }
    public int getTotalPages() { return totalPages.get(); }
    public void setTotalPages(int total) { this.totalPages.set(total); }

    public IntegerProperty pageSizeProperty() { return pageSize; }
    public int getPageSize() { return pageSize.get(); }
    public void setPageSize(int size) { this.pageSize.set(size); }

    public IntegerProperty totalRecordsProperty() { return totalRecords; }
    public void setTotalRecords(int total) { this.totalRecords.set(total); }

    public StringProperty paginationInfoProperty() { return paginationInfo; }
    public void setPaginationInfo(String info) { this.paginationInfo.set(info); }

    public BooleanProperty hasNextProperty() { return hasNext; }
    public void setHasNext(boolean value) { this.hasNext.set(value); }

    public BooleanProperty hasPreviousProperty() { return hasPrevious; }
    public void setHasPrevious(boolean value) { this.hasPrevious.set(value); }
}
