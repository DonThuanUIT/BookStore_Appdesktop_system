package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    @FXML private TableView<BookModel> bookTable;
    @FXML private TableColumn<BookModel, String> colTitle;
    @FXML private TableColumn<BookModel, String> colAuthor;
    @FXML private TableColumn<BookModel, String> colPublisher;
    @FXML private TableColumn<BookModel, Double> colPrice;
    @FXML private TableColumn<BookModel, Integer> colQuantity;
    @FXML private TableColumn<BookModel, String> colDescription;

    private InventoryInteractor interactor;
    private ObservableList<BookModel> bookList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        interactor = new InventoryInteractor();
        bookList = FXCollections.observableArrayList();

        setupTableColumns();
        loadDataFromApi();
    }

    private void setupTableColumns() {
        colTitle.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTitle()));
        colAuthor.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAuthorName()));
        colPublisher.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPublisherName()));
        colPrice.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice()));
        colQuantity.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));
        colDescription.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDescription()));

        bookTable.setItems(bookList);
    }

    private void loadDataFromApi() {
        List<BookModel> mockData = interactor.fetchMockBooks();

        bookList.clear();
        bookList.addAll(mockData);
        System.out.println("Đã nạp " + bookList.size() + " cuốn sách giả vào bảng!");
    }

    @FXML
    private void onAddNewBook() {
        System.out.println("Mở Form thêm sách...");
        // Logic mở Dialog thêm sách sẽ viết ở đây
    }
}