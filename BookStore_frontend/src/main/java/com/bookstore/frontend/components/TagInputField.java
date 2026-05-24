package com.bookstore.frontend.components;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


public class TagInputField extends FlowPane {

    private final TextField searchField;
    private final ObservableList<String> selectedTags = FXCollections.observableArrayList();
    private final ContextMenu suggestionMenu;

    private final PauseTransition debounceTimer;

    private Function<String, CompletableFuture<List<String>>> searchAsyncCallback;

    public TagInputField() {
        super();
        this.setHgap(8);
        this.setVgap(8);
        this.getStyleClass().add("tag-input-field");
        this.setStyle("-fx-padding: 8; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 5; -fx-background-color: rgba(0,0,0,0.2); -fx-min-height: 45;");

        searchField = new TextField();
        searchField.setPromptText("Gõ để tìm kiếm...");
        searchField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-pref-width: 180;");

        suggestionMenu = new ContextMenu();
        suggestionMenu.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 13px;");

        // Khởi tạo Debounce Timer (Đợi 400 mili-giây)
        debounceTimer = new PauseTransition(Duration.millis(400));

        this.getChildren().add(searchField);
        setupLogic();
    }


    public void setSearchAsyncCallback(Function<String, CompletableFuture<List<String>>> callback) {
        this.searchAsyncCallback = callback;
    }

    public List<String> getTags() {
        return new ArrayList<>(selectedTags);
    }

    public void setTags(List<String> initialTags) {
        this.getChildren().removeIf(node -> node instanceof HBox);
        selectedTags.clear();
        if (initialTags != null) {
            for (String tag : initialTags) {
                createChip(tag);
            }
        }
    }

    public void setPromptText(String text) {
        searchField.setPromptText(text);
    }

    // --- LÕI LOGIC (DEBOUNCE + ASYNC RENDERING) ---

    private void setupLogic() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Mỗi lần gõ chữ, khởi động lại Timer để tránh spam API
            debounceTimer.setOnFinished(event -> performSearch(newValue));
            debounceTimer.playFromStart();
        });

        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) suggestionMenu.hide();
        });
    }

    private void performSearch(String keyword) {
        suggestionMenu.getItems().clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            suggestionMenu.hide();
            return;
        }

        String safeKeyword = keyword.trim();

        // Hiện chữ "Đang tìm kiếm..." tạm thời để tăng trải nghiệm UX
        MenuItem loadingItem = new MenuItem("⏳ Đang tìm kiếm...");
        loadingItem.setDisable(true);
        loadingItem.setStyle("-fx-text-fill: #AAAAAA;");
        suggestionMenu.getItems().add(loadingItem);
        suggestionMenu.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);

        // Gọi Callback (Chạy API ngầm)
        if (searchAsyncCallback != null) {
            searchAsyncCallback.apply(safeKeyword).thenAccept(results -> {
                // Kết quả từ Thread mạng phải được render trên Thread UI (Platform.runLater)
                Platform.runLater(() -> renderSuggestions(safeKeyword, results));
            });
        } else {
            renderSuggestions(safeKeyword, new ArrayList<>());
        }
    }

    private void renderSuggestions(String keyword, List<String> apiResults) {
        suggestionMenu.getItems().clear();
        boolean exactMatchFound = false;

        // Render kết quả thực tế từ API
        if (apiResults != null) {
            for (String item : apiResults) {
                if (!selectedTags.contains(item)) {
                    MenuItem mi = new MenuItem(item);
                    mi.setOnAction(e -> createChip(item));
                    suggestionMenu.getItems().add(mi);
                }
                if (item.equalsIgnoreCase(keyword)) exactMatchFound = true;
            }
        }

        // Nếu API không trả về kết quả khớp 100%, tự động hiển thị nút Thêm mới
        if (!exactMatchFound && !selectedTags.contains(keyword)) {
            MenuItem addNewItem = new MenuItem("➕ Thêm mới: \"" + keyword + "\"");
            addNewItem.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
            addNewItem.setOnAction(e -> createChip(keyword));
            suggestionMenu.getItems().add(addNewItem);
        }

        if (!suggestionMenu.getItems().isEmpty()) {
            suggestionMenu.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
        } else {
            suggestionMenu.hide();
        }
    }

    private void createChip(String itemName) {
        if (selectedTags.contains(itemName)) return;

        selectedTags.add(itemName);

        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle("-fx-background-color: #4a5463; -fx-background-radius: 12; -fx-padding: 4 10; -fx-border-color: #5b677a; -fx-border-radius: 12;");

        Label lblName = new Label(itemName);
        lblName.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Button btnRemove = new Button("✕");
        btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff8a80; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 10px;");

        btnRemove.setOnAction(e -> {
            this.getChildren().remove(chip);
            selectedTags.remove(itemName);
        });

        chip.getChildren().addAll(lblName, btnRemove);

        // Chèn Chip vào vị trí ngay trước TextField
        this.getChildren().add(this.getChildren().size() - 1, chip);

        searchField.clear();
        searchField.requestFocus();
    }
}