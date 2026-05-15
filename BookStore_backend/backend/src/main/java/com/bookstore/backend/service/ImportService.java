package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.ImportDetailRequest;
import com.bookstore.backend.dto.request.ImportUpsertRequest;
import com.bookstore.backend.dto.response.ImportDetailResponse;
import com.bookstore.backend.dto.response.ImportResponse;
import com.bookstore.backend.entity.AppUser;
import com.bookstore.backend.entity.Book;
import com.bookstore.backend.entity.Import;
import com.bookstore.backend.entity.ImportDetail;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.AppUserRepository;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.ImportDetailRepository;
import com.bookstore.backend.repository.ImportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ImportService {

    private static final BigDecimal SELL_PRICE_RATE = BigDecimal.valueOf(1.05);

    private final ImportRepository importRepository;
    private final ImportDetailRepository importDetailRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository appUserRepository;

    public ImportService(ImportRepository importRepository,
                         ImportDetailRepository importDetailRepository,
                         BookRepository bookRepository,
                         AppUserRepository appUserRepository) {
        this.importRepository = importRepository;
        this.importDetailRepository = importDetailRepository;
        this.bookRepository = bookRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<ImportResponse> getAll() {
        return importRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ImportResponse> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }

        return importRepository.search(keyword.trim()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ImportResponse getById(Long id) {
        Import importOrder = findImport(id);
        return toResponse(importOrder);
    }

    @Transactional
    public ImportResponse create(ImportUpsertRequest request, String username) {
        Import importOrder = new Import();
        importOrder.setStaff(resolveStaffByUsername(username));
        Import savedImport = importRepository.save(importOrder);

        List<ImportDetail> savedDetails = request.details().stream()
                .map(detailRequest -> createDetail(savedImport, detailRequest))
                .toList();

        savedImport.setTotalCost(calculateTotalCost(savedDetails));
        return toResponse(importRepository.save(savedImport));
    }

    @Transactional
    public ImportResponse update(Long id, ImportUpsertRequest request) {
        Import importOrder = findImport(id);

        List<ImportDetail> oldDetails = importDetailRepository.findByImportOrderId(id);
        oldDetails.forEach(detail -> adjustBookStock(detail.getBook(), -detail.getQuantity(), null));
        importDetailRepository.deleteByImportOrderId(id);
        importDetailRepository.flush();

        List<ImportDetail> newDetails = request.details().stream()
                .map(detailRequest -> createDetail(importOrder, detailRequest))
                .toList();

        importOrder.setTotalCost(calculateTotalCost(newDetails));
        return toResponse(importRepository.save(importOrder));
    }

    @Transactional
    public void delete(Long id) {
        Import importOrder = findImport(id);
        List<ImportDetail> details = importDetailRepository.findByImportOrderId(id);
        details.forEach(detail -> adjustBookStock(detail.getBook(), -detail.getQuantity(), null));
        importDetailRepository.deleteByImportOrderId(id);
        importRepository.delete(importOrder);
    }

    private ImportDetail createDetail(Import importOrder, ImportDetailRequest request) {
        Book book = bookRepository.findByIdActive(request.bookId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        ImportDetail detail = new ImportDetail();
        detail.setImportOrder(importOrder);
        detail.setBook(book);
        detail.setQuantity(request.quantity());
        detail.setImportPrice(request.importPrice());

        adjustBookStock(book, request.quantity(), request.importPrice());
        return importDetailRepository.save(detail);
    }

    private void adjustBookStock(Book book, int quantityDelta, Double importPrice) {
        int currentQuantity = book.getQuantity() == null ? 0 : book.getQuantity();
        int newQuantity = currentQuantity + quantityDelta;
        if (newQuantity < 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Số lượng sách không đủ để xóa/cập nhật phiếu nhập");
        }

        book.setQuantity(newQuantity);
        if (importPrice != null) {
            book.setSellPrice(calculateSellPrice(importPrice));
        }
        bookRepository.save(book);
    }

    private BigDecimal calculateSellPrice(Double importPrice) {
        return BigDecimal.valueOf(importPrice)
                .multiply(SELL_PRICE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Double calculateTotalCost(List<ImportDetail> details) {
        return details.stream()
                .mapToDouble(detail -> detail.getQuantity() * detail.getImportPrice())
                .sum();
    }

    private Import findImport(Long id) {
        return importRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu nhập"));
    }

    private AppUser resolveStaffByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên"));
    }

    private ImportResponse toResponse(Import importOrder) {
        List<ImportDetailResponse> detailResponses = importDetailRepository.findByImportOrderId(importOrder.getId())
                .stream()
                .map(this::toDetailResponse)
                .toList();

        Long staffId = importOrder.getStaff() == null ? null : importOrder.getStaff().getId();
        String staffUsername = importOrder.getStaff() == null ? null : importOrder.getStaff().getUsername();

        return new ImportResponse(
                importOrder.getId(),
                staffId,
                staffUsername,
                importOrder.getTotalCost(),
                importOrder.getImportDate(),
                detailResponses
        );
    }

    private ImportDetailResponse toDetailResponse(ImportDetail detail) {
        Book book = detail.getBook();
        Double lineTotal = detail.getQuantity() * detail.getImportPrice();
        return new ImportDetailResponse(
                detail.getId(),
                book == null ? null : book.getId(),
                book == null ? null : book.getTitle(),
                detail.getQuantity(),
                detail.getImportPrice(),
                lineTotal
        );
    }
}
