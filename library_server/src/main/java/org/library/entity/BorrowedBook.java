package org.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "borrowed_books", uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "user_id"}))
public class BorrowedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime borrowedDate;

    private Integer borrowCount = 0;

    private LocalDateTime dueDate;

    // ✅ Actual no-argument constructor needed by JPA
    public BorrowedBook() {
    }

    // ✅ Custom constructor for manual creation
    public BorrowedBook(Book book, User user, LocalDateTime borrowedDate, Integer borrowCount, LocalDateTime dueDate) {
        this.book = book;
        this.user = user;
        this.borrowedDate = borrowedDate;
        this.borrowCount = borrowCount;
        this.dueDate = dueDate;
    }
}
