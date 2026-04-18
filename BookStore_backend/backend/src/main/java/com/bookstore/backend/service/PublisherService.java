package com.bookstore.backend.service;

import java.util.List;

import com.bookstore.backend.dto.request.PublisherUpsertRequest;
import com.bookstore.backend.dto.response.PublisherResponse;
import com.bookstore.backend.entity.Publisher;
import com.bookstore.backend.exception.AppException;
import com.bookstore.backend.repository.PublisherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Transactional(readOnly = true)
    public List<PublisherResponse> getAll() {
        return publisherRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PublisherResponse getById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
        return toResponse(publisher);
    }

    @Transactional
    public PublisherResponse create(PublisherUpsertRequest request) {
        Publisher publisher = Publisher.builder()
                .name(request.name().trim())
                .build();
        Publisher saved = publisherRepository.save(publisher);
        return toResponse(saved);
    }

    @Transactional
    public PublisherResponse update(Long id, PublisherUpsertRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản"));
        publisher.setName(request.name().trim());
        return toResponse(publisherRepository.save(publisher));
    }

    @Transactional
    public void delete(Long id) {
        if (!publisherRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà xuất bản");
        }
        publisherRepository.deleteById(id);
    }

    private PublisherResponse toResponse(Publisher publisher) {
        return new PublisherResponse(
                publisher.getId(),
                publisher.getName()
        );
    }
}
