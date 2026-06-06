package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.ShopInteractor;
import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.ShopModel;
import com.bookstore.frontend.model.dto.Response.CategoryResponseDto;
import com.bookstore.frontend.service.api.ApiClient;
import com.bookstore.frontend.util.CartStore;
import com.bookstore.frontend.util.LoadingUtils;
import com.bookstore.frontend.util.PaginationSynchronizer;
import com.bookstore.frontend.util.PaginationUtil;
import com.bookstore.frontend.utils.AlertUtils;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShopController {

    @FXML private TextField txtSearch;
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private ComboBox<String> cbSort;
    @FXML private FlowPane booksContainer;
    @FXML private Label lblStatus;
    @FXML private Label lblPaginationInfo; // Thêm Label này vào FXML để hiển thị trang

    @FXML private TextField txtCategorySearch;
    @FXML private VBox categoriesContainer;

    @FXML private BookDetailSidePanelController bookDetailSidePanelController;

    private ShopModel model;
    private ShopInteractor interactor;
    private List<BookModel> originalBooksList = new ArrayList<>();
    private List<BookModel> currentFilteredList = new ArrayList<>(); // Lưu danh sách sau khi đã lọc

    private List<String> allCategoryNames = new ArrayList<>();
    private final Set<String> activeSelectedCategories = new HashSet<>();

    private int currentPage = 0;
    private static final int PAGE_SIZE = 10; // Cố định 10 items/trang để load nhanh

    private static final String DEFAULT_COVER_URL = "https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg";

    public ShopController() {
        this.model = new ShopModel();
        this.interactor = new ShopInteractor(this.model);
    }

    @FXML
    public void initialize() {
        setupUI();
        setupRealTimeFilters();
        setupPaginationSync();  // Thêm đồng bộ hóa phân trang
        loadCategoriesFromApi();
        loadInitialData();
        setupRealTimeSync();
    }
    
    /**
     * Thiết lập đồng bộ hóa phân trang với các page khác
     */
    private void setupPaginationSync() {
        PaginationSynchronizer.getInstance().addListener((pageType, page, pageSize) -> {
            if ("SHOP".equals(pageType)) {
                currentPage = page;
                updatePaginationUI();
            }
        });
    }

    private void setupUI() {
        if (cbSort != null) {
            cbSort.setItems(FXCollections.observableArrayList("Newest", "Price: Low to High", "Price: High to Low"));
            cbSort.setValue("Newest");
        }
    }

    private void loadCategoriesFromApi() {
        ApiClient.getInstance().get("/categories").thenAccept(res -> {
            if (res.statusCode() == 200) {
                try {
                    JsonNode root = ApiClient.getInstance().getMapper().readTree(res.body());
                    List<CategoryResponseDto> dtoList = ApiClient.getInstance().getMapper().readerForListOf(CategoryResponseDto.class).readValue(root);
                    allCategoryNames = dtoList.stream().map(CategoryResponseDto::getName).toList();
                    Platform.runLater(() -> renderCategoryCheckboxes(""));
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void renderCategoryCheckboxes(String filterText) {
        if (categoriesContainer == null) return;
        categoriesContainer.getChildren().clear();
        String lowerFilter = filterText == null ? "" : filterText.toLowerCase();
        for (String categoryName : allCategoryNames) {
            if (categoryName.toLowerCase().contains(lowerFilter)) {
                CheckBox cb = new CheckBox(categoryName);
                cb.setStyle("-fx-text-fill: #AAAAAA; -fx-padding: 2 0 2 0;");
                cb.setSelected(activeSelectedCategories.contains(categoryName));
                cb.selectedProperty().addListener((obs, old, isSelected) -> {
                    if (isSelected) activeSelectedCategories.add(categoryName);
                    else activeSelectedCategories.remove(categoryName);
                    executeFilter();
                });
                categoriesContainer.getChildren().add(cb);
            }
        }
    }

    private void loadInitialData() {
        System.out.println("[ShopController.loadInitialData] Bắt đầu load dữ liệu...");
        LoadingUtils.show("Đang tải sách...");
        
        interactor.getBooksPage(0, 100).thenAccept(pageDto -> {
            System.out.println("[ShopController.loadInitialData] Nhận dữ liệu từ Interactor: " + pageDto.getContent().size() + " sách");
            Platform.runLater(() -> {
                originalBooksList = pageDto.getContent();
                System.out.println("[ShopController.loadInitialData] Thực thi filter...");
                executeFilter();
                LoadingUtils.hide();
            });
        }).exceptionally(ex -> {
            System.err.println("[ShopController.loadInitialData] Exception: " + ex.getMessage());
            ex.printStackTrace();
            LoadingUtils.hide();
            return null;
        });
    }

    private void setupRealTimeFilters() {
        txtSearch.textProperty().addListener((obs, old, newVal) -> executeFilter());
        txtMinPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());
        txtMaxPrice.textProperty().addListener((obs, old, newVal) -> executeFilter());
        cbSort.valueProperty().addListener((obs, old, newVal) -> executeFilter());
        if (txtCategorySearch != null) txtCategorySearch.textProperty().addListener((obs, old, newVal) -> renderCategoryCheckboxes(newVal));
    }

    private void executeFilter() {
        currentPage = 0; // Reset về trang đầu khi có lọc
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim() : "";
        final List<String> selectedCategories = new ArrayList<>(activeSelectedCategories);

        if (keyword.isEmpty()) {
            currentFilteredList = interactor.applyClientSideFilters(
                    originalBooksList, "", selectedCategories,
                    parseDoubleSafe(txtMinPrice.getText()), parseDoubleSafe(txtMaxPrice.getText()), cbSort.getValue()
            );
            updatePaginationUI();
        } else {
            LoadingUtils.show("Đang tìm kiếm...");
            interactor.searchBooksFromBackend(keyword).thenAccept(booksFromBackend -> {
                Platform.runLater(() -> {
                    currentFilteredList = interactor.applyClientSideFilters(
                            booksFromBackend, "", selectedCategories,
                            parseDoubleSafe(txtMinPrice.getText()), parseDoubleSafe(txtMaxPrice.getText()), cbSort.getValue()
                    );
                    updatePaginationUI();
                    LoadingUtils.hide();
                });
            }).exceptionally(ex -> {
                LoadingUtils.hide();
                return null;
            });
        }
    }

    private void updatePaginationUI() {
        int totalPages = PaginationUtil.calculateTotalPages(currentFilteredList.size(), PAGE_SIZE);
        currentPage = PaginationUtil.clampPage(currentPage, totalPages);
        
        int from = PaginationUtil.getFromIndex(currentPage, PAGE_SIZE);
        int to = PaginationUtil.getToIndex(currentPage, PAGE_SIZE, currentFilteredList.size());
        
        // Render books cho trang hiện tại
        if (from < currentFilteredList.size()) {
            renderBooks(currentFilteredList.subList(from, to));
        } else {
            renderBooks(new ArrayList<>());
        }

        // Cập nhật label phân trang
        if (lblPaginationInfo != null) {
            lblPaginationInfo.setText(PaginationUtil.formatShopPaginationInfo(currentPage, PAGE_SIZE, currentFilteredList.size()));
        }
    }

    @FXML private void handleNextPage() {
        int totalPages = PaginationUtil.calculateTotalPages(currentFilteredList.size(), PAGE_SIZE);
        if (PaginationUtil.hasNextPage(currentPage, totalPages)) {
            currentPage++;
            updatePaginationUI();
            PaginationSynchronizer.getInstance().setShopPage(currentPage, PAGE_SIZE);
        }
    }

    @FXML private void handlePrevPage() {
        if (PaginationUtil.hasPreviousPage(currentPage)) {
            currentPage--;
            updatePaginationUI();
            PaginationSynchronizer.getInstance().setShopPage(currentPage, PAGE_SIZE);
        }
    }

    private void renderBooks(List<BookModel> books) {
        booksContainer.getChildren().clear();
        for (BookModel book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/components/BookCard.fxml"));
                Node cardNode = loader.load();
                BookCardController cardController = loader.getController();
                cardController.setBookData(book.getTitle(), book.getFormattedAuthors(), String.format("%,.0f đ", book.getPrice()),
                        (book.getImageUrl() != null && !book.getImageUrl().isBlank()) ? book.getImageUrl() : DEFAULT_COVER_URL);
                cardController.setCallbacks(
                        () -> bookDetailSidePanelController.setBookDetailDataAndShow(book),
                        () -> AlertUtils.promptQuantityForCart(book.getTitle()).ifPresent(qty -> CartStore.getInstance().addBook(book, qty))
                );
                booksContainer.getChildren().add(cardNode);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private Double parseDoubleSafe(String text) {
        try { return (text == null || text.isBlank()) ? null : Double.parseDouble(text.trim()); } catch (Exception e) { return null; }
    }

    private void setupRealTimeSync() {
        ApiClient.getInstance().onBookUpdated(updatedBook -> Platform.runLater(this::executeFilter));
        ApiClient.getInstance().onBookDeleted(bookId -> Platform.runLater(this::executeFilter));
    }
}