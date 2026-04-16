package com.bookstore.backend.service;

import com.bookstore.backend.dto.request.BookUpsertRequest;
import com.bookstore.backend.dto.response.BookResponse;
import com.bookstore.backend.entity.Book;
import com.bookstore.backend.entity.Category;
import com.bookstore.backend.entity.Publisher;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.CategoryRepository;
import com.bookstore.backend.repository.PublisherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, PublisherRepository publisherRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
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
        Book book = bookRepository.findByIdActive(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));
        return toResponse(book);
    }

    @Transactional
    public BookResponse create(BookUpsertRequest request) {
        Publisher publisher = null;
        if (request.publisherId() != null) {
            publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
        }

        Set<Category> categories = resolveCategories(request.categoryIds());

        // IMPORTANT: avoid using Lombok builder because it may null-out default field initializers
        // (e.g., isDeleted). Using new Book() preserves entity defaults.
        Book book = new Book();
        book.setTitle(request.title().trim());
        book.setPublishYear(request.publishYear());
        book.setSellPrice(request.sellPrice());
        book.setImageUrl(request.imageUrl());
        book.setPublisher(publisher);
        if (categories != null) {
            book.setCategories(categories);
        }

        Book saved = bookRepository.save(book);
        return toResponse(saved);
    }

    @Transactional
    public BookResponse update(Long id, BookUpsertRequest request) {
        Book book = bookRepository.findByIdActive(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        book.setTitle(request.title().trim());

        // Update only when the request provides a non-null value to avoid accidental clearing.


        if (request.publishYear() != null) {
            book.setPublishYear(request.publishYear());
        }
        if (request.sellPrice() != null) {
            book.setSellPrice(request.sellPrice());
        }
        if (request.imageUrl() != null) {
            book.setImageUrl(request.imageUrl());
        }
        if (request.publisherId() != null) {
            Publisher publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
            book.setPublisher(publisher);
        }

        if (request.categoryIds() != null) {
            Set<Category> categories = resolveCategories(request.categoryIds());
            book.setCategories(categories);
        }

        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findByIdActive(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        book.setIsDeleted(true);
        bookRepository.save(book);
    }

    @Transactional
    public BookResponse createWithTitleAndImage(String title, String imageUrl) {
        return create(new BookUpsertRequest(title, null, null, imageUrl, null, null));
    }

    private BookResponse toResponse(Book book) {
        Long publisherId = book.getPublisher() != null ? book.getPublisher().getId() : null;
        String publisherName = book.getPublisher() != null ? book.getPublisher().getName() : null;

        List<Long> categoryIds = (book.getCategories() == null)
                ? List.of()
                : book.getCategories().stream().map(Category::getId).toList();

        List<String> categoryNames = (book.getCategories() == null)
                ? List.of()
                : book.getCategories().stream().map(Category::getName).toList();

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getPublishYear(),
                book.getSellPrice(),
                book.getImageUrl(),
                book.getIsDeleted(),
                publisherId,
                publisherName,
                categoryIds,
                categoryNames
        );
    }

    private Set<Category> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null) {
            return null; // caller means: do not change categories
        }

        Set<Long> uniqueIds = new HashSet<>(categoryIds);
        if (uniqueIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(uniqueIds));
        if (categories.size() != uniqueIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục sách");
        }
        return categories;
    }
}

