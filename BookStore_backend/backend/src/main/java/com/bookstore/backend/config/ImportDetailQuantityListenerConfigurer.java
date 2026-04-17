package com.bookstore.backend.config;

import com.bookstore.backend.listener.ImportDetailQuantityListener;
import com.bookstore.backend.repository.BookRepository;
import com.bookstore.backend.repository.ImportDetailRepository;
import org.springframework.stereotype.Component;

@Component
public class ImportDetailQuantityListenerConfigurer {

    public ImportDetailQuantityListenerConfigurer(
            BookRepository bookRepository,
            ImportDetailRepository importDetailRepository) {
        ImportDetailQuantityListener.setRepositories(bookRepository, importDetailRepository);
    }
}
