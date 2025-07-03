package org.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.library.entity.Book;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

 // New method to support searching by title, author, or ID
 List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrId(String title, String author, Long id);
 // Method to search by title or author
 List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
}