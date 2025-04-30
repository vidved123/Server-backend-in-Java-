package org.library.repository;

import org.library.dto.ViewBorrrowedBooksdto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewBorrowedBooksRepository {
    List<ViewBorrrowedBooksdto> findBorrowedBooks(Long currentUserId, String role, String bookTitle, Long searchUserId);
}
