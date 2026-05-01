package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.InventoryInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.utils.AlertUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
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
    @FXML private TableColumn<BookModel, Void> colAction; // Đã thêm cột Action

    private InventoryInteractor interactor;
    private ObservableList<BookModel> bookList;
    private BookFormController lastOpenedFormController;


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

        // --- CÀI ĐẶT NÚT 3 CHẤM CHO CỘT ACTION ---
        colAction.setCellFactory(param -> new TableCell<>() {
            private final MenuButton menuButton = new MenuButton("•••");
            private final MenuItem editItem = new MenuItem("Edit");
            private final MenuItem deleteItem = new MenuItem("Delete");

            {
                // Xóa viền nền để nút 3 chấm trông tự nhiên
                menuButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;");
                menuButton.getItems().addAll(editItem, deleteItem);

                // Bắt sự kiện khi bấm nút Edit
                editItem.setOnAction(event -> {
                    BookModel selectedBook = getTableView().getItems().get(getIndex());
                    onEditBook(selectedBook);
                });

                // Bắt sự kiện khi bấm nút Delete
                deleteItem.setOnAction(event -> {
                    BookModel selectedBook = getTableView().getItems().get(getIndex());
                    onDeleteBook(selectedBook);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(menuButton);
                }
            }
        });

        bookTable.setItems(bookList);
    }

    private void loadDataFromApi() {
        new Thread(() -> {
            try {
                // Tạm thời gọi hàm fetchAllBooks (bạn cần đảm bảo hàm này có sẵn trong Interactor nhé)
                List<BookModel> realData = interactor.fetchAllBooks();
                Platform.runLater(() -> {
                    bookList.clear();
                    bookList.addAll(realData);
                });
            } catch (Exception e) {
                System.err.println("Lỗi khi kéo API: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void onAddNewBook() {
        BookModel newBook = new BookModel();
        boolean saved = showBookForm(newBook, false);
        if (saved) {
            // Mở luồng ngầm để gọi API tránh đơ màn hình
            new Thread(() -> {
                try {
                    // Lấy ra Controller của Dialog để trích xuất file ảnh
                    BookFormController formCtrl = lastOpenedFormController; // Đọc mẹo bên dưới

                    interactor.saveBook(newBook, formCtrl.getSelectedImageFile(), false);

                    Platform.runLater(() -> {
                        System.out.println("Thêm thành công!");
                        loadDataFromApi(); // Kéo lại dữ liệu mới nhất hiển thị lên bảng
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> System.err.println("Lỗi Thêm sách: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void onEditBook(BookModel selectedBook) {
        boolean saved = showBookForm(selectedBook, true);
        if (saved) {
            new Thread(() -> {
                try {
                    BookFormController formCtrl = lastOpenedFormController;
                    interactor.saveBook(selectedBook, formCtrl.getSelectedImageFile(), true);

                    Platform.runLater(() -> {
                        System.out.println("Cập nhật thành công!");
                        loadDataFromApi();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> System.err.println("Lỗi Cập nhật: " + e.getMessage()));
                }
            }).start();
        }
    }

    /**
     * Hàm dùng chung để mở cửa sổ Dialog
     */
    private boolean showBookForm(BookModel book, boolean isEdit) {
        try {
            // 1. Nạp file FXML của Form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/BookFormView.fxml"));
            VBox page = loader.load();

            // 2. Tạo một Stage (Cửa sổ) mới
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isEdit ? "Edit Book" : "Add New Book");
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Bắt buộc tương tác xong mới được quay lại
            dialogStage.initOwner(bookTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // 3. Truyền dữ liệu vào Controller của Form
            BookFormController controller = loader.getController();
            this.lastOpenedFormController = controller;
            controller.setBook(book, isEdit);

            // 4. Hiển thị cửa sổ và đợi cho đến khi nó đóng lại
            dialogStage.showAndWait();

            // 5. Kiểm tra xem người dùng nhấn "Save" hay "Cancel"
            if (controller.isSaveClicked()) {
                // Nếu người dùng nhấn Save, ta lấy thêm file ảnh họ đã chọn
                java.io.File imageFile = controller.getSelectedImageFile();
                // Tạm thời in ra để kiểm tra
                System.out.println("Sẵn sàng gửi dữ liệu của: " + book.getTitle());
                if (imageFile != null) System.out.println("Ảnh đính kèm: " + imageFile.getAbsolutePath());

                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.show(Alert.AlertType.ERROR, "Error", "Could not load book form.");
        }
        return false;
    }

    private void onDeleteBook(BookModel book) {
        // Logic xóa sẽ làm ở bước sau
    }
}