package org.library.repository;

import org.library.entity.BorrowedBook;
import org.library.entity.Book;
import org.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@SuppressWarnings("unused")
@Repository
public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Long> {

    List<BorrowedBook> findByBook_TitleContainingIgnoreCaseOrBook_AuthorContainingIgnoreCase(String title, String author);

    long countByUserId(Long userId);

    List<BorrowedBook> findByUserId(Long userId);

    List<BorrowedBook> findByBookAndUser(Book book, User user);

    long countByBookAndUser(Book book, User user);

    // ✅ Add these two
    List<BorrowedBook> findByBookIdAndUserId(Long bookId, Long userId);

    long countByBookIdAndUserId(Long bookId, Long userId);
}
