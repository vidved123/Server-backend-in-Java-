package org.library.service;

import org.library.dto.ViewBorrowedBooksdto;

import java.util.List;

@SuppressWarnings("unused")
public interface ViewBorrowedBooksService {

    List<ViewBorrowedBooksdto> getBorrowedBooks(Long currentUserId, String role);

    // Method to search borrowed books by userId for admins
    List<ViewBorrowedBooksdto> searchBorrowedBooksByUserId(Long userId);

    // Method to search borrowed books by title or author (for normal users)
    List<ViewBorrowedBooksdto> searchBorrowedBooks(String searchQuery, Long userId, String role);
}