package com.bookstore.backend.controller;

import com.bookstore.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books")
public class BookController {

    @GetMapping
    public String getAllBooks() {
        return "list of books.";
    }

    @Autowired
    private ImageService imageService;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "New books with cover art added.")
    public ResponseEntity<?> addBook(
            @RequestPart("title") String title,
            @RequestPart("image") MultipartFile image) {
        try{
            String imageUrl = imageService.uploadImage(image);
            return ResponseEntity.ok("Success! Book title: " + title + ". Image URL: " + imageUrl);
        }catch (IOException e){
            e.printStackTrace();
            return ResponseEntity.status(500).body("Image upload error: " + e.getMessage());
        }
    }








}
