package com.bookstore.backend.controller;

import java.util.List;

import com.bookstore.backend.dto.request.PublisherUpsertRequest;
import com.bookstore.backend.dto.response.PublisherResponse;
import com.bookstore.backend.service.PublisherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publishers")
@Tag(name = "Publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping
    public ResponseEntity<List<PublisherResponse>> getAll() {
        return ResponseEntity.ok(publisherService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(publisherService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<PublisherResponse> create(@Valid @RequestBody PublisherUpsertRequest request) {
        PublisherResponse created = publisherService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<PublisherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PublisherUpsertRequest request
    ) {
        return ResponseEntity.ok(publisherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        publisherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
