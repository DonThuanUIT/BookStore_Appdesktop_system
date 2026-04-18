package com.bookstore.backend.service;

import java.util.List;

import com.bookstore.backend.dto.request.AuthorUpsertRequest;
import com.bookstore.backend.dto.response.AuthorResponse;
import com.bookstore.backend.entity.Author;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.AuthorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> getAll() {
        return authorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AuthorResponse getById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy tác giả"));
        return toResponse(author);
    }

    @Transactional
    public AuthorResponse create(AuthorUpsertRequest request) {
        Author author = Author.builder()
                .name(request.name().trim())
                .build();
        Author saved = authorRepository.save(author);
        return toResponse(saved);
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorUpsertRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy tác giả"));
        author.setName(request.name().trim());
        return toResponse(authorRepository.save(author));
    }

    @Transactional
    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy tác giả");
        }
        authorRepository.deleteById(id);
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getName()
        );
    }
}
