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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bookstore.backend.exception.AppException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final ImageService imageService;
    public BookService(BookRepository bookRepository,
                       PublisherRepository publisherRepository,
                       CategoryRepository categoryRepository,
                       AuthorRepository authorRepository,
                       ImageService imageService) {
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.imageService = imageService;
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
    public BookResponse create(BookUpsertRequest request, MultipartFile image) {

        if (request == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Request không hợp lệ");
        }

        // Nếu có ảnh → tạo request mới
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageService.uploadImage(image);

            request = new BookUpsertRequest(
                    request.title(),
                    request.publishYear(),
                    request.sellPrice(),
                    imageUrl,
                    request.publisherId(),
                    request.authorIds(),
                    request.categoryIds()
            );
        }

        return create(request);
    }

    @Transactional
    public BookResponse update(Long id, BookUpsertRequest request) {
        Book book = bookRepository.findByIdActive(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

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
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
            book.setPublisher(publisher);
        }

        if (request.categoryIds() != null) {
            Set<Category> categories = resolveCategories(request.categoryIds());
            book.setCategories(categories);
        }

        if (request.authorIds() != null) {
            Set<Author> authors = resolveAuthors(request.authorIds());
            book.setAuthors(authors);
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

    @Transactional
    private BookResponse create(BookUpsertRequest request) {
        Book book = new Book();
        book.setTitle(request.title().trim());
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
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
            book.setPublisher(publisher);
        }
        if (request.categoryIds() != null) {
            Set<Category> categories = resolveCategories(request.categoryIds());
            book.setCategories(categories);
        }
        if (request.authorIds() != null) {
            Set<Author> authors = resolveAuthors(request.authorIds());
            book.setAuthors(authors);
        }
        // quantity default handled by entity
        Book saved = bookRepository.save(book);
        return toResponse(saved);
    }

    @Transactional
    public BookResponse createWithTitleAndImage(String title, String imageUrl) {
        return create(new BookUpsertRequest(title, null, null, imageUrl, null, null, null));
    }

    private BookResponse toResponse(Book book) {
        Long publisherId = book.getPublisher() != null ? book.getPublisher().getId() : null;
        String publisherName = book.getPublisher() != null ? book.getPublisher().getName() : null;
        Integer quantity = book.getQuantity() != null ? book.getQuantity() : 0;

        List<Long> categoryIds = (book.getCategories() == null)
                ? List.of()
                : book.getCategories().stream().map(Category::getId).toList();

        List<String> categoryNames = (book.getCategories() == null)
                ? List.of()
                : book.getCategories().stream().map(Category::getName).toList();

        List<Long> authorIds = (book.getAuthors() == null)
                ? List.of()
                : book.getAuthors().stream().map(Author::getId).toList();

        List<String> authorNames = (book.getAuthors() == null)
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
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục sách");
        }
        return categories;
    }

    private Set<Author> resolveAuthors(List<Long> authorIds) {
        if (authorIds == null) {
            return null; // caller means: do not change authors
        }

        Set<Long> uniqueIds = new HashSet<>(authorIds);
        if (uniqueIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Author> authors = new HashSet<>(authorRepository.findAllById(uniqueIds));
        if (authors.size() != uniqueIds.size()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy tác giả sách");
        }
        return authors;
    }
}

