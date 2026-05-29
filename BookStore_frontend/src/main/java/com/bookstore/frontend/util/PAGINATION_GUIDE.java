/**
 * HƯỚNG DẪN SỬ DỤNG HỆ THỐNG PHÂN TRANG ĐỒNG BỘ (Pagination Synchronizer)
 * 
 * ============================================================================
 * 1. KIẾN TRÚC CỦA HỆ THỐNG PHÂN TRANG
 * ============================================================================
 * 
 * Hệ thống gồm 3 thành phần chính:
 * 
 * A. PaginationUtil (Tiện ích phân trang)
 *    - Tính toán các chỉ số trang
 *    - Format thông tin phân trang
 *    - Kiểm tra trang tiếp theo/trước đó
 * 
 * B. LoadingUtils (Hiện thị loading spinner)
 *    - Hiển thị/ẩn loading spinner ở trung tâm màn hình
 *    - Hỗ trợ nested loading (counter-based)
 *    - Thực thi task với loading spinner tự động
 * 
 * C. PaginationSynchronizer (Đồng bộ hóa phân trang)
 *    - Đồng bộ phân trang giữa 3 trang: SHOP, INVENTORY, IMPORT_HISTORY
 *    - Sử dụng pattern Observer
 *    - Tự động thông báo khi trang thay đổi
 * 
 * ============================================================================
 * 2. CÁC TRANG HỖ TRỢ PHÂN TRANG
 * ============================================================================
 * 
 * 1. Shop (ShopController)
 *    - PAGE_SIZE: 12 sách mỗi trang
 *    - Loại: Client-side pagination (Filter trên dữ liệu local)
 *    - Sync Type: "SHOP"
 * 
 * 2. Inventory (InventoryController)
 *    - PAGE_SIZE: 10 sách mỗi trang
 *    - Loại: Client-side pagination
 *    - Sync Type: "INVENTORY"
 * 
 * 3. OrderHistory (OrderHistoryController)
 *    - PAGE_SIZE: 20 orders mỗi trang
 *    - Loại: Server-side pagination (từ API)
 *    - Sync Type: "IMPORT_HISTORY" (dùng chung với ImportHistory)
 * 
 * ============================================================================
 * 3. CÁCH SỬ DỤNG TRONG CONTROLLER
 * ============================================================================
 * 
 * // A. Khởi tạo PaginationSynchronizer trong initialize()
 * @Override
 * public void initialize() {
 *     // ... setup khác ...
 *     setupPaginationSync();  // Gọi method này
 * }
 * 
 * // B. Method setupPaginationSync() - Đăng ký listener
 * private void setupPaginationSync() {
 *     PaginationSynchronizer.getInstance().addListener((pageType, page, pageSize) -> {
 *         if ("SHOP".equals(pageType)) {  // Hoặc "INVENTORY", "IMPORT_HISTORY"
 *             currentPage = page;
 *             updateUI();
 *         }
 *     });
 * }
 * 
 * // C. Xử lý click nút Next/Prev
 * @FXML
 * private void handleNextPage() {
 *     if (PaginationUtil.hasNextPage(currentPage, totalPages)) {
 *         currentPage++;
 *         updateUI();
 *         // Thông báo cho các trang khác thay đổi
 *         PaginationSynchronizer.getInstance().setShopPage(currentPage, PAGE_SIZE);
 *     }
 * }
 * 
 * @FXML
 * private void handlePrevPage() {
 *     if (PaginationUtil.hasPreviousPage(currentPage)) {
 *         currentPage--;
 *         updateUI();
 *         PaginationSynchronizer.getInstance().setShopPage(currentPage, PAGE_SIZE);
 *     }
 * }
 * 
 * ============================================================================
 * 4. CÁCH SỬ DỤNG LOADING SPINNER
 * ============================================================================
 * 
 * // A. Khởi tạo một lần từ MainController
 * LoadingUtils.initLoadingLayer(mainStackPane);
 * 
 * // B. Hiển thị loading
 * LoadingUtils.show("Đang tải dữ liệu...");
 * 
 * // C. Ẩn loading
 * LoadingUtils.hide();
 * 
 * // D. Thực thi task với loading tự động
 * LoadingUtils.executeWithLoading("Đang tải...", () -> {
 *     // Code của bạn ở đây
 *     return null;
 * }, () -> {
 *     // Callback khi hoàn thành
 *     System.out.println("Hoàn thành!");
 * });
 * 
 * // E. Async version
 * LoadingUtils.executeWithLoadingAsync("Đang tải...", () -> {
 *     // Code của bạn
 *     return result;
 * }).thenAccept(result -> {
 *     System.out.println("Result: " + result);
 * });
 * 
 * // F. Loading với độ trễ (tránh flickering)
 * LoadingUtils.showWithDelay("Đang tải...", 300); // Chờ 300ms rồi mới show
 * 
 * ============================================================================
 * 5. VÍ DỤ HOÀN CHỈNH: SHOP CONTROLLER
 * ============================================================================
 * 
 * private int currentPage = 0;
 * private static final int PAGE_SIZE = 12;
 * private List<BookModel> currentFilteredList = new ArrayList<>();
 * 
 * @FXML
 * public void initialize() {
 *     // ... setup UI ...
 *     setupPaginationSync();
 *     loadInitialData();
 * }
 * 
 * private void setupPaginationSync() {
 *     PaginationSynchronizer.getInstance().addListener((pageType, page, pageSize) -> {
 *         if ("SHOP".equals(pageType)) {
 *             currentPage = page;
 *             updatePaginationUI();
 *         }
 *     });
 * }
 * 
 * private void loadInitialData() {
 *     LoadingUtils.show("Đang tải sách...");
 *     
 *     // API call
 *     api.getBooks().thenAccept(books -> {
 *         Platform.runLater(() -> {
 *             currentFilteredList = books;
 *             updatePaginationUI();
 *             LoadingUtils.hide();
 *         });
 *     }).exceptionally(ex -> {
 *         LoadingUtils.hide();
 *         return null;
 *     });
 * }
 * 
 * private void updatePaginationUI() {
 *     int totalPages = PaginationUtil.calculateTotalPages(
 *         currentFilteredList.size(), PAGE_SIZE);
 *     
 *     // Render items cho trang hiện tại
 *     int from = PaginationUtil.getFromIndex(currentPage, PAGE_SIZE);
 *     int to = PaginationUtil.getToIndex(currentPage, PAGE_SIZE, 
 *         currentFilteredList.size());
 *     
 *     List<BookModel> pageItems = currentFilteredList.subList(from, to);
 *     renderBooks(pageItems);
 *     
 *     // Cập nhật label thông tin
 *     lblPaginationInfo.setText(
 *         PaginationUtil.formatShopPaginationInfo(currentPage, PAGE_SIZE, 
 *             currentFilteredList.size())
 *     );
 *     
 *     // Disable/enable nút
 *     btnNext.setDisable(!PaginationUtil.hasNextPage(currentPage, totalPages));
 *     btnPrev.setDisable(!PaginationUtil.hasPreviousPage(currentPage));
 * }
 * 
 * @FXML
 * private void handleNextPage() {
 *     int totalPages = PaginationUtil.calculateTotalPages(
 *         currentFilteredList.size(), PAGE_SIZE);
 *     if (PaginationUtil.hasNextPage(currentPage, totalPages)) {
 *         currentPage++;
 *         updatePaginationUI();
 *         PaginationSynchronizer.getInstance().setShopPage(currentPage, PAGE_SIZE);
 *     }
 * }
 * 
 * @FXML
 * private void handlePrevPage() {
 *     if (PaginationUtil.hasPreviousPage(currentPage)) {
 *         currentPage--;
 *         updatePaginationUI();
 *         PaginationSynchronizer.getInstance().setShopPage(currentPage, PAGE_SIZE);
 *     }
 * }
 * 
 * ============================================================================
 * 6. LOADING SPINNER FXML TEMPLATE
 * ============================================================================
 * 
 * Sử dụng file LoadingSpinner.fxml:
 * 
 * <StackPane style="-fx-background-color: rgba(0, 0, 0, 0.5);">
 *     <VBox alignment="CENTER" spacing="15.0">
 *         <ProgressIndicator fx:id="progressIndicator" style="-fx-accent: #FFC107;" />
 *         <Label fx:id="lblLoadingText" text="Đang tải dữ liệu..." />
 *     </VBox>
 * </StackPane>
 * 
 * ============================================================================
 * 7. LOADER INITIALIZATION (MainController)
 * ============================================================================
 * 
 * @FXML private StackPane mainContainer;  // Root container
 * 
 * @FXML
 * public void initialize() {
 *     // Khởi tạo loading layer
 *     LoadingUtils.initLoadingLayer(mainContainer);
 *     
 *     // ... setup khác ...
 * }
 * 
 * ============================================================================
 * 8. PAGINATION STATE METHODS
 * ============================================================================
 * 
 * // Lấy trang hiện tại
 * int shopPage = PaginationSynchronizer.getInstance().getShopPage();
 * 
 * // Kiểm tra đồng bộ hóa có bật
 * boolean syncEnabled = PaginationSynchronizer.getInstance().isSyncEnabled();
 * 
 * // Bật/tắt đồng bộ hóa
 * PaginationSynchronizer.getInstance().setSyncEnabled(true);
 * 
 * // Reset tất cả trang
 * PaginationSynchronizer.getInstance().resetAllPages();
 * 
 * // Reset một trang
 * PaginationSynchronizer.getInstance().resetPage("SHOP");
 * 
 * ============================================================================
 * 9. BEST PRACTICES
 * ============================================================================
 * 
 * 1. Luôn khởi tạo LoadingUtils.initLoadingLayer() một lần từ main controller
 * 2. Sử dụng nested counter cho loading (LoadingUtils tự động quản lý)
 * 3. Đảm bảo updateUI() được gọi trên JavaFX thread (Platform.runLater)
 * 4. Gọi PaginationSynchronizer.setXXXPage() SAU khi updateUI()
 * 5. Sử dụng LoadingUtils.show/hide cho tất cả async operations
 * 6. Kiểm tra page bounds trước khi render
 * 7. Reset currentPage = 0 khi filter/search thay đổi
 * 
 * ============================================================================
 * 10. DEBUGGING TIPS
 * ============================================================================
 * 
 * // Kiểm tra loading counter
 * System.out.println("Loading counter: " + LoadingUtils.getLoadingCounter());
 * 
 * // Kiểm tra loading có hiển thị
 * System.out.println("Is loading: " + LoadingUtils.isShowing());
 * 
 * // In thông tin phân trang
 * System.out.println(PaginationUtil.formatPaginationInfo(
 *     currentPage, PAGE_SIZE, totalItems));
 * 
 * ============================================================================
 */
