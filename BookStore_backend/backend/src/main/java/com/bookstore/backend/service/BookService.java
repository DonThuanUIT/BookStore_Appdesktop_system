package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.BookUpsertRequest;
import com.bookstore.backend.dto.response.BookResponse;
import com.bookstore.backend.entity.Book;
import com.bookstore.backend.entity.Category;
import com.bookstore.backend.entity.Publisher;
import com.bookstore.backend.entity.Author;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.CategoryRepository;
import com.bookstore.backend.repository.PublisherRepository;
import com.bookstore.backend.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.bookstore.backend.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;

    private final SseNotificationService sseNotificationService;


    public BookService(BookRepository bookRepository,
                       PublisherRepository publisherRepository,
                       CategoryRepository categoryRepository,
                       AuthorRepository authorRepository,
                       SseNotificationService sseNotificationService) {
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.sseNotificationService = sseNotificationService;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAll() {
        return bookRepository.findAllActive()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        Book book = bookRepository.findDetailsByIdActive(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));
        return toResponse(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getByName(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        return bookRepository.findByTitleContainingIgnoreCaseActiveOrderByTitleAsc(trimmed)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional
    public BookResponse create(BookUpsertRequest request) {

        if (request == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Request không hợp lệ");
        }

        Book book = new Book();

        // Title
        book.setTitle(request.title() != null ? request.title().trim() : null);

        // Publish Year
        if (request.publishYear() != null) {
            book.setPublishYear(request.publishYear());
        }

        book.setQuantity(0);
        book.setSellPrice(request.sellPrice() != null ? request.sellPrice() : BigDecimal.ZERO);

        // Image URL
        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            book.setImageUrl(request.imageUrl());
        }

        if (request.description() != null) {
            book.setDescription(request.description().trim());
        }

        if (request.sellPrice() != null) {
            book.setSellPrice(request.sellPrice());
        }

        // Publisher
        if (request.publisherId() != null) {
            Publisher publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
            book.setPublisher(publisher);
        }

        // Categories
        if (request.categoryIds() != null) {
            book.setCategories(resolveCategories(request.categoryIds()));
        }

        // Authors
        if (request.authorIds() != null) {
            book.setAuthors(resolveAuthors(request.authorIds()));
        }

        Book saved = bookRepository.save(book);

        BookResponse response = toResponse(saved);
        try {
            sseNotificationService.sendNotification("UPDATE_BOOK", response);
        } catch (Exception e) {
            System.err.println("Lỗi gửi SSE: " + e.getMessage());
        }
        return toResponse(saved);
    }


    @Transactional
    public BookResponse update(Long id, BookUpsertRequest request) {
        Book book = bookRepository.findByIdActive(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        if (request.title() != null) {
            book.setTitle(request.title().trim());
        }

        if (request.publishYear() != null) {
            book.setPublishYear(request.publishYear());
        }

        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            book.setImageUrl(request.imageUrl());
        }

        if (request.description() != null) {
            book.setDescription(request.description().trim());
        }

        if (request.publisherId() != null) {
            Publisher publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
            book.setPublisher(publisher);
        }

        if (request.categoryIds() != null) {
            book.setCategories(resolveCategories(request.categoryIds()));
        }

        if (request.authorIds() != null) {
            book.setAuthors(resolveAuthors(request.authorIds()));
        }

        Book saved = bookRepository.save(book);
        BookResponse response = toResponse(saved);

        try {
            sseNotificationService.sendNotification("UPDATE_BOOK", response);
        } catch (Exception e) {
            System.err.println("Lỗi gửi SSE: " + e.getMessage());
        }

        return response;
    }


    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findByIdActive(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        book.setIsDeleted(true);
        bookRepository.save(book);

        try {
            sseNotificationService.sendNotification("DELETE_BOOK", id);
        } catch (Exception e) {
            System.err.println("Lỗi gửi SSE: " + e.getMessage());
        }
    }


    private BookResponse toResponse(Book book) {
        Long publisherId = book.getPublisher() != null ? book.getPublisher().getId() : null;
        String publisherName = book.getPublisher() != null ? book.getPublisher().getName() : null;
        Integer quantity = book.getQuantity() != null ? book.getQuantity() : 0;

        List<Long> categoryIds = book.getCategories() == null
                ? List.of()
                : book.getCategories().stream().map(Category::getId).toList();

        List<String> categoryNames = book.getCategories() == null
                ? List.of()
                : book.getCategories().stream().map(Category::getName).toList();

        List<Long> authorIds = book.getAuthors() == null
                ? List.of()
                : book.getAuthors().stream().map(Author::getId).toList();

        List<String> authorNames = book.getAuthors() == null
                ? List.of()
                : book.getAuthors().stream().map(Author::getName).toList();

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getPublishYear(),
                book.getSellPrice(),
                book.getImageUrl(),
                book.getDescription(),
                book.getIsDeleted(),
                quantity,
                publisherId,
                publisherName,
                authorIds,
                authorNames,
                categoryIds,
                categoryNames
        );
    }


    private Set<Category> resolveCategories(List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(uniqueIds));

        if (categories.size() != uniqueIds.size()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục sách");
        }
        return categories;
    }

    private Set<Author> resolveAuthors(List<Long> ids) {
        Set<Long> uniqueIds = new HashSet<>(ids);
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(uniqueIds));

        if (authors.size() != uniqueIds.size()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy tác giả");
        }
        return authors;
    }

    // THÊM ANNOTATION NÀY ĐỂ KÍCH HOẠT LAZY LOADING TRONG LUỒNG PHÂN TRANG
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(int page, int size, String sortBy, String direction){
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending(): Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Book> bookPage = bookRepository.findAllActive(pageable);

        return bookPage.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }

        return bookRepository.searchActive(keyword.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }
}

