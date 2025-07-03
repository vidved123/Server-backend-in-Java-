package org.library.repository;

import org.library.dto.ViewBorrowedBooksdto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewBorrowedBooksRepository {
    List<ViewBorrowedBooksdto> findBorrowedBooks(Long currentUserId, String role, String bookTitle, Long searchUserId);
}