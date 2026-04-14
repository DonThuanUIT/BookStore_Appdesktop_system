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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
    public static void showView(String fxmlFileName, String title) throws IOException {
        String resourcePath = fxmlFileName.startsWith("/") ? fxmlFileName : VIEW_BASE_PATH + fxmlFileName;
        URL fxmlLocation = MainApplication.class.getResource(resourcePath);
=======
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("view/MainLayout.fxml"));
>>>>>>> origin/main

        if (fxmlLocation == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

<<<<<<< HEAD
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1300, 650);

        URL cssLocation = MainApplication.class.getResource(CSS_PATH);
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        }

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
=======
        stage.setTitle("BookStore - Login");
        stage.setScene(scene);
        stage.show();
>>>>>>> origin/main
    }

    public static void main(String[] args) {
        launch(args);
    }
}