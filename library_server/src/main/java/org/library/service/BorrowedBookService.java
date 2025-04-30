package org.library.service;

import org.library.dto.BorrowedBookdto;

import java.util.List;

public interface BorrowedBookService {

    void borrowBooksForUser(List<Long> bookIds, Long userId);

    List<BorrowedBookdto> getUserBorrowedBooks(Long userId);

    void returnBooksForUser(List<Long> bookIds, Long userId);

    List<BorrowedBookdto> getAllBorrowedBooks();

    List<BorrowedBookdto> searchUserBorrowedBooksByTitleOrAuthor(Long userId, String query);
}
