package org.library.service.impl;

import org.library.dto.ViewBorrrowedBooksdto;
import org.library.entity.BorrowedBook;
import org.library.entity.enums.Role;
import org.library.repository.BorrowedBookRepository;
import org.library.service.ViewBorrowedBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewBorrowedBooksServiceImpl implements ViewBorrowedBooksService {

    @Autowired
    private BorrowedBookRepository borrowedBookRepository;

    @Override
    public List<ViewBorrrowedBooksdto> getBorrowedBooks(Long userId, String role) {
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            List<BorrowedBook> borrowedBooks;

            // Fetch borrowed books based on user role
            if (roleEnum == Role.ADMIN) {
                borrowedBooks = borrowedBookRepository.findAll();
            } else {
                borrowedBooks = borrowedBookRepository.findByUserId(userId);
            }

            // Filter duplicates, if necessary
            borrowedBooks = borrowedBooks.stream()
                    .distinct() // Remove duplicates if the same book has been borrowed multiple times
                    .collect(Collectors.toList());

            return borrowedBooks.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role, e);
        }
    }

    @Override
    public List<ViewBorrrowedBooksdto> searchBorrowedBooksByUserId(Long userId) {
        return List.of();  // Empty list for now, can be implemented as needed
    }

    @Override
    public List<ViewBorrrowedBooksdto> searchBorrowedBooks(String searchQuery, Long userId, String role) {
        Role roleEnum = Role.valueOf(role.toUpperCase());
        List<BorrowedBook> borrowedBooks;

        // Search borrowed books by title or author based on role
        if (roleEnum == Role.ADMIN) {
            borrowedBooks = borrowedBookRepository.findByBook_TitleContainingIgnoreCaseOrBook_AuthorContainingIgnoreCase(searchQuery, searchQuery);
        } else {
            borrowedBooks = borrowedBookRepository.findByUserId(userId);
            borrowedBooks = borrowedBooks.stream()
                    .filter(book -> book.getBook().getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            book.getBook().getAuthor().toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter duplicates, if necessary
        borrowedBooks = borrowedBooks.stream()
                .distinct() // Remove duplicates
                .collect(Collectors.toList());

        return borrowedBooks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ViewBorrrowedBooksdto convertToDto(BorrowedBook borrowedBook) {
        ViewBorrrowedBooksdto dto = new ViewBorrrowedBooksdto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Set book details
        dto.setBookId(borrowedBook.getBook().getId());
        dto.setTitle(borrowedBook.getBook().getTitle());
        dto.setAuthor(borrowedBook.getBook().getAuthor());

        // Format and set dates
        dto.setBorrowedDate(borrowedBook.getBorrowedDate().format(formatter));  // Ensure borrowedDate is a LocalDate or LocalDateTime
        dto.setDueDate(borrowedBook.getDueDate().format(formatter));  // Ensure dueDate is a LocalDate or LocalDateTime

        // Set username for admin role
        if (borrowedBook.getUser() != null) {
            dto.setUsername(borrowedBook.getUser().getUsername());
        }

        return dto;
    }
}
