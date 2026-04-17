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
import com.bookstore.backend.entity.*;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository,
                       PublisherRepository publisherRepository,
                       CategoryRepository categoryRepository,
                       AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
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
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));
        return toResponse(book);
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

        // Price
        if (request.sellPrice() != null) {
            book.setSellPrice(request.sellPrice());
        }

        // Image URL
        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            book.setImageUrl(request.imageUrl());
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

        if (request.sellPrice() != null) {
            book.setSellPrice(request.sellPrice());
        }

        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            book.setImageUrl(request.imageUrl());
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

        return toResponse(bookRepository.save(book));
    }


    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findByIdActive(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

        book.setIsDeleted(true);
        bookRepository.save(book);
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

    public Page<Book> getAllBooks(int page, int size, String sortBy, String direction){
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending(): Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.findAll(pageable);
    }
}

}
