package org.library.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.library.entity.Book;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service
public class BookMasterService {

    private final EntityManager entityManager;

    public BookMasterService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Fetch Book Master Data (Total Copies and Available Copies)
    public List<Book> getBooksMaster() {
        // Native SQL query to get book data with total and available copies
        String query = "SELECT b.title, b.author, " +
                "COUNT(i.id) AS totalCopies, " +
                "SUM(CASE WHEN i.status = 'available' THEN 1 ELSE 0 END) AS availableCopies " +
                "FROM Books b " +
                "LEFT JOIN Inventory i ON b.id = i.book_id " +
                "GROUP BY b.title, b.author";

        Query nativeQuery = entityManager.createNativeQuery(query);

        // Execute the query and get the result list
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = nativeQuery.getResultList();

        // Convert the result into a list of Book objects
        return resultList.stream().map(result -> {
            Book book = new Book();
            book.setTitle((String) result[0]);  // Title from query result
            book.setAuthor((String) result[1]); // Author from query result
            book.setTotalCopies(((Number) result[2]).intValue()); // Total Copies from query result
            book.setAvailableCopies(((Number) result[3]).intValue()); // Available Copies from query result
            return book;
        }).collect(Collectors.toList());
    }
}
