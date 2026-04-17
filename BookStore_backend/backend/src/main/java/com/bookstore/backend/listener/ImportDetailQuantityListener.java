package com.bookstore.backend.listener;

import com.bookstore.backend.entity.ImportDetail;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.ImportDetailRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

public class ImportDetailQuantityListener {

    private static BookRepository bookRepository;
    private static ImportDetailRepository importDetailRepository;

    public static void setRepositories(BookRepository bookRepo, ImportDetailRepository importDetailRepo) {
        bookRepository = bookRepo;
        importDetailRepository = importDetailRepo;
    }

    @PostPersist
    @PostUpdate
    @PostRemove
    public void syncBookQuantity(ImportDetail detail) {
        if (bookRepository == null || importDetailRepository == null) {
            return;
        }
        if (detail.getBook() == null || detail.getBook().getId() == null) {
            return;
        }
        Long bookId = detail.getBook().getId();
        Long sum = importDetailRepository.sumQuantityByBookId(bookId);
        int quantity = sum == null ? 0 : sum.intValue();
        bookRepository.findById(bookId).ifPresent(book -> {
            book.setQuantity(quantity);
            bookRepository.save(book);
        });
    }
}
