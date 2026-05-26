package com.bookstore.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;

public class MainApplication extends Application {
    private static final String VIEW_BASE_PATH = "/com/bookstore/frontend/";
    private static final String THEME_STYLESHEET = "/com/bookstore/frontend/css/theme.css";
    private static final Path SOURCE_RESOURCES_PATH = Path.of("src", "main", "resources", "com", "bookstore", "frontend");
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showView("view/LoginView.fxml", "BookStore - Login");
    }

    public static void showView(String fxmlPath, String title) throws IOException {
        URL resource = resolveViewResource(fxmlPath);
        if (resource == null) {
            throw new IOException("FXML resource not found: " + fxmlPath);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1300, 600);
        URL stylesheet = resolveStylesheetResource();
        if (stylesheet != null && !scene.getStylesheets().contains(stylesheet.toExternalForm())) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String resolveResourcePath(String fxmlPath) {
        if (fxmlPath.startsWith("/")) {
            return fxmlPath;
        }
        return VIEW_BASE_PATH + fxmlPath;
    }

    private static URL resolveViewResource(String fxmlPath) throws IOException {
        String normalizedPath = fxmlPath.startsWith("/") ? fxmlPath : VIEW_BASE_PATH + fxmlPath;
        URL classpathResource = MainApplication.class.getResource(normalizedPath);
        if (classpathResource != null) return classpathResource;

        Path sourceResource = SOURCE_RESOURCES_PATH.resolve(fxmlPath.replace("/", java.io.File.separator));
        if (Files.exists(sourceResource)) return sourceResource.toUri().toURL();

        return null;
    }

    private static URL resolveStylesheetResource() throws IOException {
        URL classpathResource = MainApplication.class.getResource(THEME_STYLESHEET);
        if (classpathResource != null) {
            return classpathResource;
        }

        Path sourceStylesheet = SOURCE_RESOURCES_PATH.resolve(Path.of("css", "theme.css"));
        if (Files.exists(sourceStylesheet)) {
            return sourceStylesheet.toUri().toURL();
        }

        return null;
    }
}
