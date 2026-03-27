package com.bookstore.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApplication extends Application {

    // Thêm 'throws IOException' vì việc đọc file FXML có thể bị lỗi không tìm thấy file
    @Override
    public void start(Stage stage) throws IOException {

        // 1. Chỉ đường cho FXMLLoader tìm đến file giao diện
        // Vì MainApplication đang ở com.bookstore.frontend,
        // nó sẽ tự động tìm vào đúng thư mục tương ứng bên resources rồi đi tiếp vào "view/"
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("view/LoginView.fxml"));

        // 2. Tải toàn bộ cấu trúc XML lên thành Scene (Khung cảnh)
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);

        // 3. Đặt tiêu đề và hiển thị Cửa sổ (Stage)
        stage.setTitle("Đăng nhập - BookStore App");
        stage.setScene(scene);
        stage.setResizable(false); // Khóa không cho người dùng kéo giãn cửa sổ đăng nhập
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}