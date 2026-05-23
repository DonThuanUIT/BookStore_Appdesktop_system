package com.bookstore.backend.controller;

import com.bookstore.backend.dto.request.CategoryUpsertRequest;
import com.bookstore.backend.dto.response.CategoryResponse;
import com.bookstore.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable @Positive(message = "id is invalid") Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<CategoryResponse>> getByName(
            @PathVariable @NotBlank(message = "name is required") String name
    ) {
        return ResponseEntity.ok(categoryService.getByName(name));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryUpsertRequest request) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable @Positive(message = "id is invalid") Long id,
            @Valid @RequestBody CategoryUpsertRequest request
    ) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id is invalid") Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
