package com.bookstore.backend.controller;

import com.bookstore.backend.dto.request.AuthorUpsertRequest;
import com.bookstore.backend.dto.response.AuthorResponse;
import com.bookstore.backend.service.AuthorService;
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
@RequestMapping("/api/authors")
@Tag(name = "Authors")
@Validated
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAll() {
        return ResponseEntity.ok(authorService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getById(@PathVariable @Positive(message = "id is invalid") Long id) {
        return ResponseEntity.ok(authorService.getById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<AuthorResponse>> getByName(
            @PathVariable @NotBlank(message = "name is required") String name
    ) {
        return ResponseEntity.ok(authorService.getByName(name));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorUpsertRequest request) {
        AuthorResponse created = authorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponse> update(
            @PathVariable @Positive(message = "id is invalid") Long id,
            @Valid @RequestBody AuthorUpsertRequest request
    ) {
        return ResponseEntity.ok(authorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "id is invalid") Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
