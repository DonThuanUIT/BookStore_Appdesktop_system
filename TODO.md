# TODO - Export Excel cho Revenue

- [x] Backend: Add endpoint `GET /api/revenue/export?year=` trong `RevenueController`
- [x] Backend: Implement tạo file Excel bằng Apache POI trong `RevenueExportService`



- [x] Backend: Thêm dependency `poi-ooxml` vào `BookStore_backend/backend/pom.xml`

- [x] Frontend: Thêm nút `XUẤT EXCEL` vào `RevenueReportView.fxml`
- [x] Frontend: Thêm handler `handleExportExcel()` trong `RevenueReportController`
- [x] Frontend: Thêm method `exportRevenueToExcel(int year)` trong `RevenueApiService`
- [x] Frontend: Dùng `FileChooser` để lưu file `.xlsx` tải từ backend

- [ ] Chạy build/test: kiểm tra file Excel tạo ra đúng dữ liệu

