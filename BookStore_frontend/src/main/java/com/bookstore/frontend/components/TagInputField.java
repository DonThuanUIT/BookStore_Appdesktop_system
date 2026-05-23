package com.bookstore.frontend.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Component: Khung nhập liệu hỗ trợ Tag/Chip và Suggestion.
 * Có thể tái sử dụng ở mọi màn hình (Shop, Import, Inventory...)
 */
public class TagInputField extends FlowPane {

    private final TextField searchField;
    // Dùng ObservableList để bên ngoài có thể lắng nghe sự thay đổi (nếu cần)
    private final ObservableList<String> selectedTags = FXCollections.observableArrayList();
    private List<String> suggestionsPool = new ArrayList<>();

    public TagInputField() {
        super();
        this.setHgap(8);
        this.setVgap(8);
        this.getStyleClass().add("tag-input-field");
        // Giữ lại style gốc để không bị lỗi UI, sau này có thể đẩy ra CSS
        this.setStyle("-fx-padding: 8; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 5; -fx-background-color: rgba(0,0,0,0.2); -fx-min-height: 45;");

        searchField = new TextField();
        searchField.setPromptText("Gõ để tìm kiếm...");
        searchField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-pref-width: 180;");

        this.getChildren().add(searchField);
        setupLogic();
    }

    // --- CÁC HÀM GIAO TIẾP VỚI BÊN NGOÀI (PUBLIC API) ---

    /** Cung cấp danh sách các từ khóa gợi ý (Master Data từ DB) */
    public void setSuggestionsPool(List<String> suggestions) {
        this.suggestionsPool = suggestions != null ? suggestions : new ArrayList<>();
    }

    /** Lấy danh sách các thẻ (Tag) người dùng đã chọn */
    public List<String> getTags() {
        return new ArrayList<>(selectedTags);
    }

    /** Set danh sách Tag có sẵn (Dùng khi mở Form Edit) */
    public void setTags(List<String> initialTags) {
        // Clear UI cũ
        this.getChildren().removeIf(node -> node instanceof HBox);
        selectedTags.clear();

        if (initialTags != null) {
            for (String tag : initialTags) {
                createChip(tag);
            }
        }
    }

    /** Thay đổi dòng chữ mờ (Prompt text) */
    public void setPromptText(String text) {
        searchField.setPromptText(text);
    }


    private void setupLogic() {
        ContextMenu suggestionMenu = new ContextMenu();
        suggestionMenu.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 13px;");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            suggestionMenu.getItems().clear();

            if (newValue == null || newValue.trim().isEmpty()) {
                suggestionMenu.hide();
                return;
            }

            String searchText = newValue.trim().toLowerCase();
            boolean exactMatchFound = false;

            for (String item : suggestionsPool) {
                if (item.toLowerCase().contains(searchText) && !selectedTags.contains(item)) {
                    MenuItem mi = new MenuItem(item);
                    mi.setOnAction(e -> createChip(item));
                    suggestionMenu.getItems().add(mi);
                    if (item.equalsIgnoreCase(searchText)) exactMatchFound = true;
                }
            }

            if (!exactMatchFound && !selectedTags.contains(newValue.trim())) {
                MenuItem addNewItem = new MenuItem("➕ Thêm mới: \"" + newValue.trim() + "\"");
                addNewItem.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
                addNewItem.setOnAction(e -> createChip(newValue.trim()));
                suggestionMenu.getItems().add(addNewItem);
            }

            if (!suggestionMenu.getItems().isEmpty()) {
                suggestionMenu.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
            } else {
                suggestionMenu.hide();
            }
        });

        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) suggestionMenu.hide();
        });
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

        // Chèn vào vị trí trước TextField
        this.getChildren().add(this.getChildren().size() - 1, chip);

        searchField.clear();
        searchField.requestFocus();
    }
}