package org.library.service;

import org.library.dto.ViewBorrrowedBooksdto;

import java.util.List;

public interface ViewBorrowedBooksService {

    List<ViewBorrrowedBooksdto> getBorrowedBooks(Long currentUserId, String role);

    // Method to search borrowed books by userId for admins
    List<ViewBorrrowedBooksdto> searchBorrowedBooksByUserId(Long userId);

    // Method to search borrowed books by title or author (for normal users)
    List<ViewBorrrowedBooksdto> searchBorrowedBooks(String searchQuery, Long userId, String role);
}
