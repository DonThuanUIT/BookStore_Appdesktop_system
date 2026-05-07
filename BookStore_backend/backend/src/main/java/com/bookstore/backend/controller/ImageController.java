package com.bookstore.backend.controller;

import com.bookstore.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "API manages image uploads to Cloudinary")
public class ImageController {

    private final ImageService imageService;

    // Tuân thủ SOLID: Constructor Injection
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(summary = "Upload a new image", description = "Receive file from client and return image URL after successfully uploading to Cloudinary")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = imageService.uploadImage(file);

        return ResponseEntity.ok(Map.of("url", url));
    }
}