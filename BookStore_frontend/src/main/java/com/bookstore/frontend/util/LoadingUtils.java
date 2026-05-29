package com.bookstore.frontend.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LoadingUtils {
    
    private static StackPane loadingLayer;
    private static StackPane loadingPane;
    private static com.bookstore.frontend.controller.LoadingController loadingController;
    private static int loadingCounter = 0; // Đếm số lần gọi show/hide để tránh ẩn sớm
    
    /**
     * Khởi tạo loading overlay (gọi một lần từ MainController)
     */
    public static void initLoadingLayer(StackPane container) {
        try {
            FXMLLoader loader = new FXMLLoader(LoadingUtils.class.getResource("/com/bookstore/frontend/view/components/LoadingSpinner.fxml"));
            loadingPane = loader.load();
            loadingController = loader.getController();
            loadingLayer = container;
            
            // Thêm loading vào container
            container.getChildren().add(loadingPane);
            
            // Ban đầu ẩn
            loadingPane.setVisible(false);
            loadingPane.setManaged(false);
            loadingCounter = 0;
        } catch (IOException e) {
            System.err.println("Lỗi load LoadingSpinner.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị loading spinner ở trung tâm màn hình
     */
    public static void show(String message) {
        loadingCounter++;
        Platform.runLater(() -> {
            if (loadingPane != null) {
                // Đảm bảo loading pane được đặt ở trung tâm
                loadingPane.setStyle(loadingPane.getStyle() + "; -fx-alignment: CENTER;");
                loadingPane.setVisible(true);
                loadingPane.setManaged(true);
                
                // Đưa loading lên phía trước (toFront)
                loadingPane.toFront();
                
                if (loadingController != null) {
                    loadingController.setLoadingText(message);
                }
            }
        });
    }
    
    /**
     * Ẩn loading spinner
     */
    public static void hide() {
        loadingCounter--;
        
        // Chỉ ẩn khi counter = 0 (tất cả show đã được hide)
        if (loadingCounter <= 0) {
            loadingCounter = 0;
            Platform.runLater(() -> {
                if (loadingPane != null) {
                    loadingPane.setVisible(false);
                    loadingPane.setManaged(false);
                }
            });
        }
    }
    
    /**
     * Hiển thị loading spinner ngay lập tức (có độ trễ nhất định)
     * để tránh flickering với các tác vụ nhanh
     */
    public static void showWithDelay(String message, long delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                show(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Thực thi task với loading spinner
     */
    public static <T> void executeWithLoading(String message, java.util.concurrent.Callable<T> task, Runnable onComplete) {
        show(message);
        
        new Thread(() -> {
            try {
                task.call();
            } catch (Exception e) {
                System.err.println("Lỗi trong task: " + e.getMessage());
                e.printStackTrace();
            } finally {
                hide();
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            }
        }).start();
    }
    
    /**
     * Thực thi task với loading spinner và trả về CompletableFuture
     */
    public static <T> CompletableFuture<T> executeWithLoadingAsync(String message, java.util.concurrent.Callable<T> task) {
        show(message);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                System.err.println("Lỗi trong task async: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                hide();
            }
        });
    }
    
    /**
     * Kiểm tra loading có đang hiển thị không
     */
    public static boolean isShowing() {
        return loadingPane != null && loadingPane.isVisible();
    }
    
    /**
     * Lấy số lần show hiện tại
     */
    public static int getLoadingCounter() {
        return loadingCounter;
    }
}
