package com.bookstore.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    private static Stage primaryStage;
    private static final String VIEW_BASE_PATH = "/com/bookstore/frontend/view/";
    private static final String CSS_PATH = "/com/bookstore/frontend/css/theme.css";

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            showView("LoginView.fxml", "BookStore - Login");
           // showView("MainLayout.fxml", "BookStore - Main");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showView(String fxmlFileName, String title) throws IOException {
        // Logic của bạn: Tự động nối VIEW_BASE_PATH nếu file name không bắt đầu bằng "/"
        String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : VIEW_BASE_PATH + fxmlFileName;
        URL fxmlLocation = MainApplication.class.getResource(resourcePath);

        if (fxmlLocation == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        // Nạp FXML theo fxmlLocation đã xác định
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        // Thiết lập Scene với kích thước 1300x650 như code của bạn
        Scene scene = new Scene(root, 1300, 600);

        // Nạp CSS theme
        URL cssLocation = MainApplication.class.getResource(CSS_PATH);
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        }

        // Cấu hình Stage theo ý bạn
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}