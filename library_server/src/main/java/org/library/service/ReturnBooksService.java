package org.library.service;

import org.library.dto.ReturnBooksdto;
import org.library.entity.Book;
import org.library.entity.BorrowedBook;
import org.library.entity.Inventory;
import org.library.entity.enums.Status;
import org.library.exception.BookNotFoundException;
import org.library.repository.BorrowedBookRepository;
import org.library.repository.BookRepository;
import org.library.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Service for handling book returns
@Service
public class ReturnBooksService {

    private static final Logger logger = LoggerFactory.getLogger(ReturnBooksService.class);

    private final BorrowedBookRepository borrowedBookRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Constructor to inject the repositories
    public ReturnBooksService(BorrowedBookRepository borrowedBookRepository,
                              BookRepository bookRepository,
                              InventoryRepository inventoryRepository) {
        this.borrowedBookRepository = borrowedBookRepository;
        this.bookRepository = bookRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // Method to retrieve borrowed books based on the userId and role
    public List<ReturnBooksdto> getBorrowedBooks(Long userId, String role) {
        List<BorrowedBook> borrowedBooks = "admin".equalsIgnoreCase(role)
                ? borrowedBookRepository.findAll()
                : borrowedBookRepository.findByUserId(userId);

        return borrowedBooks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert BorrowedBook to ReturnBooksdto
    private ReturnBooksdto convertToDto(BorrowedBook borrowedBook) {
        ReturnBooksdto dto = new ReturnBooksdto();
        dto.setBookId(borrowedBook.getBook().getId());
        dto.setTitle(borrowedBook.getBook().getTitle());
        dto.setAuthor(borrowedBook.getBook().getAuthor());
        dto.setBorrowedDate(borrowedBook.getBorrowedDate().toString());
        dto.setDueDate(borrowedBook.getDueDate().toString());

        if (borrowedBook.getUser() != null) {
            dto.setUsername(borrowedBook.getUser().getUsername());
            dto.setUserId(borrowedBook.getUser().getId());
        }

        return dto;
    }

    // Method for returning books
    @Transactional
    public void returnBooks(List<Long> bookIds, List<Long> userIds, Long userId, String role) {
        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException("📕 Book list is empty or null.");
        }

        // If the role is admin, ensure that userIds match the bookIds
        if ("admin".equalsIgnoreCase(role) && (userIds == null || bookIds.size() != userIds.size())) {
            throw new IllegalArgumentException("🛑 Admin must provide a matching userId for each book.");
        }

        // Process each book ID
        for (int i = 0; i < bookIds.size(); i++) {
            Long bookId = bookIds.get(i);
            Long targetUserId = "admin".equalsIgnoreCase(role) ? Objects.requireNonNull(userIds).get(i) : userId;

            // Find the borrowed book for the given bookId and targetUserId
            List<BorrowedBook> borrowedBooks = borrowedBookRepository.findByBookIdAndUserId(bookId, targetUserId);

            if (borrowedBooks.isEmpty()) {
                logger.warn("⚠️ No borrowed book found for bookId {} and userId {}", bookId, targetUserId);
                continue;
            }

            if (borrowedBooks.size() > 1) {
                logger.warn("⚠️ Multiple borrowed books found for bookId {} and userId {}. Returning the first one.", bookId, targetUserId);
            }

            BorrowedBook borrowedBook = borrowedBooks.getFirst();

            if (borrowedBook.getBook() != null && borrowedBook.getUser() != null) {
                // Remove the borrowed book record
                borrowedBookRepository.delete(borrowedBook);
                logger.info("✅ Borrowed book returned: BookID={}, UserID={}", bookId, targetUserId);

                // Update the book's available copies
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new BookNotFoundException("❌ Book not found: ID=" + bookId));

                if (book.getAvailableCopies() < book.getTotalCopies()) {
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                    bookRepository.save(book);
                    logger.info("📘 Book '{}' now has {} available copies.", book.getTitle(), book.getAvailableCopies());
                } else {
                    logger.warn("❌ Available copies for book '{}' already at maximum.", book.getTitle());
                }

                // Update the inventory status
                List<Inventory> inventories = inventoryRepository.findByBookId(bookId);
                if (!inventories.isEmpty()) {
                    Inventory inventory = inventories.getFirst();
                    inventory.setStatus(Status.AVAILABLE);  // Update inventory status to AVAILABLE
                    inventoryRepository.save(inventory);
                    logger.info("📦 Inventory status updated to AVAILABLE for book '{}'", book.getTitle());
                } else {
                    logger.warn("❌ No inventory found for book '{}'", book.getTitle());
                }
            }
        }
    }
}
