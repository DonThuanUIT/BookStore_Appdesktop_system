package com.bookstore.frontend.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ImportModel {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty staffUsername = new SimpleStringProperty();
    private final StringProperty importDate = new SimpleStringProperty();
    private final DoubleProperty totalCost = new SimpleDoubleProperty();

    // Danh sách các sách bên trong phiếu nhập này
    private final ObservableList<ImportDetailModel> details = FXCollections.observableArrayList();

    public long getId() { return id.get(); }
    public void setId(long value) { id.set(value); }
    public LongProperty idProperty() { return id; }

    public String getStaffUsername() { return staffUsername.get(); }
    public void setStaffUsername(String value) { staffUsername.set(value); }
    public StringProperty staffUsernameProperty() { return staffUsername; }

    public String getImportDate() { return importDate.get(); }
    public void setImportDate(String value) { importDate.set(value); }
    public StringProperty importDateProperty() { return importDate; }

    public double getTotalCost() { return totalCost.get(); }
    public void setTotalCost(double value) { totalCost.set(value); }
    public DoubleProperty totalCostProperty() { return totalCost; }

    public ObservableList<ImportDetailModel> getDetails() { return details; }
}