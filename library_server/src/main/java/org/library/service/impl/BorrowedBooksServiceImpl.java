package org.library.service.impl;

import org.library.dto.BorrowedBookdto;
import org.library.entity.Book;
import org.library.entity.BorrowedBook;
import org.library.entity.User;
import org.library.repository.BookRepository;
import org.library.repository.BorrowedBookRepository;
import org.library.repository.UserRepository;
import org.library.service.BorrowedBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service
public class BorrowedBooksServiceImpl implements BorrowedBookService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowedBooksServiceImpl.class);

    @Autowired
    private BorrowedBookRepository borrowedBookRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void borrowBooksForUser(List<Long> bookIds, Long userId) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Iterate through the list of books to borrow
        for (Long bookId : bookIds) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            logger.info("Borrowing book '{}'. Current available copies: {}", book.getTitle(), book.getAvailableCopies());

            // Check if there are available copies
            if (book.getAvailableCopies() <= 0) {
                throw new RuntimeException("Book '" + book.getTitle() + "' is not available.");
            }

            // Check if the user already has this book borrowed
            List<BorrowedBook> existingBorrows = borrowedBookRepository.findByBookAndUser(book, user);
            if (!existingBorrows.isEmpty()) {
                throw new RuntimeException("User already borrowed the book '" + book.getTitle() + "'");
            }

            // Save the borrow record
            BorrowedBook borrowedBook = new BorrowedBook();
            borrowedBook.setBook(book);
            borrowedBook.setUser(user);
            borrowedBook.setBorrowedDate(LocalDate.now().atStartOfDay());
            borrowedBook.setDueDate(LocalDate.now().plusDays(14).atStartOfDay());
            borrowedBook.setBorrowCount(1);  // Assuming this count tracks the borrow attempts

            borrowedBookRepository.save(borrowedBook);

            // Update the available copies
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);

            logger.info("Book '{}' borrowed. New available copies: {}", book.getTitle(), book.getAvailableCopies());
        }
    }

    @Override
    @Transactional
    public void returnBooksForUser(List<Long> bookIds, Long userId) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("User '{}' is returning books: {}", userId, bookIds);

        for (Long bookId : bookIds) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            // Fetch borrowed books by the user for the specific book
            List<BorrowedBook> borrowedBooks = borrowedBookRepository.findByBookAndUser(book, user);

            if (borrowedBooks.isEmpty()) {
                throw new RuntimeException("No borrowed record found for book ID " + bookId + " and user " + userId);
            }

            for (BorrowedBook borrowedBook : borrowedBooks) {
                // Increment available copies for the book
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                bookRepository.save(book);

                // Delete the borrowed book record
                borrowedBookRepository.delete(borrowedBook);

                logger.info("Returned book '{}'. Updated available copies: {}", book.getTitle(), book.getAvailableCopies());
            }
        }
    }

    @Override
    public List<BorrowedBookdto> getUserBorrowedBooks(Long userId) {
        return borrowedBookRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowedBookdto> getAllBorrowedBooks() {
        return borrowedBookRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowedBookdto> searchUserBorrowedBooksByTitleOrAuthor(Long userId, String query) {
        return borrowedBookRepository.findByUserId(userId).stream()
                .filter(bb -> bb.getBook().getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        bb.getBook().getAuthor().toLowerCase().contains(query.toLowerCase()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper method to map borrowed book to DTO
    private BorrowedBookdto mapToDto(BorrowedBook borrowedBook) {
        BorrowedBookdto dto = new BorrowedBookdto();

        dto.setBookId(borrowedBook.getBook().getId());
        dto.setTitle(borrowedBook.getBook().getTitle());
        dto.setAuthor(borrowedBook.getBook().getAuthor());
        dto.setBorrowedDate(borrowedBook.getBorrowedDate().toLocalDate());
        dto.setDueDate(borrowedBook.getDueDate().toLocalDate());
        dto.setUsername(borrowedBook.getUser().getUsername());

        long overdueDays = ChronoUnit.DAYS.between(dto.getDueDate(), LocalDate.now());
        dto.setOverdueDays(overdueDays > 0 ? (int) overdueDays : 0);
        dto.setFine(dto.getOverdueDays() * 1.0); // $1/day fine

        return dto;
    }
}
