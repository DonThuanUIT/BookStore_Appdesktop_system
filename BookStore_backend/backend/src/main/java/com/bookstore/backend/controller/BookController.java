package com.bookstore.backend.controller;

import com.bookstore.backend.dto.request.BookUpsertRequest;
import com.bookstore.backend.dto.response.BookResponse;
import com.bookstore.backend.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books")
@Validated
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable @Positive(message = "id is invalid") Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<BookResponse>> getByName(
            @PathVariable @NotBlank(message = "name is required") String name
    ) {
        return ResponseEntity.ok(bookService.getByName(name));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> search(
            @RequestParam(required = false)
            @Size(max = 100, message = "keyword must not exceed 100 characters")
            String keyword
    ) {
        return ResponseEntity.ok(bookService.search(keyword));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookUpsertRequest request) {
        BookResponse created = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BookResponse> update(
            @PathVariable @Positive(message = "id is invalid") Long id,
            @Valid @RequestBody BookUpsertRequest request
    ) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id is invalid") Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<BookResponse>> getBooks(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must be greater than or equal to 0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 100, message = "size must not exceed 100")
            int size,

            @RequestParam(defaultValue = "id")
            @Pattern(regexp = "^(id|title|publishYear|sellPrice|quantity)$", message = "sortBy is invalid")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            @Pattern(regexp = "^(?i)(asc|desc)$", message = "direction must be asc or desc")
            String direction
    ) {
        return ResponseEntity.ok(bookService.getAllBooks(page, size, sortBy, direction));
    }
}
