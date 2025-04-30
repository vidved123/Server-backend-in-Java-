package org.library.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.library.repository.BookRepository;
import org.library.repository.BorrowedBookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DatabaseInitService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitService.class);

    private final BookRepository bookRepository;
    private final BorrowedBookRepository borrowedBookRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private boolean isInitialized = false;

    public DatabaseInitService(
            BookRepository bookRepository,
            BorrowedBookRepository borrowedBookRepository
    ) {
        this.bookRepository = bookRepository;
        this.borrowedBookRepository = borrowedBookRepository;
    }

    @PostConstruct
    public void initDatabase() {
        if (isInitialized) return;

        try {
            logger.info("Starting Database Initialization...");
            entityManager.clear();

            long bookCount = bookRepository.count();
            long borrowedBookCount = borrowedBookRepository.count();

            logger.info("Books in DB: {}", bookCount);
            logger.info("Borrowed Books in DB: {}", borrowedBookCount);

            logger.info("✅ Database initialized successfully.");
        } catch (Exception e) {
            logger.error("❌ Database Initialization Failed: {}", e.getMessage(), e);
        }

        isInitialized = true;
    }
}
