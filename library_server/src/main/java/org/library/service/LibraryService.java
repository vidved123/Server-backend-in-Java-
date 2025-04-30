package org.library.service;

import org.library.dto.BorrowedBookdto;
import org.library.entity.BorrowedBook;
import org.library.repository.BookRepository;
import org.library.repository.BorrowedBookRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final BorrowedBookRepository borrowedBookRepository;

    public LibraryService(BookRepository bookRepository, BorrowedBookRepository borrowedBookRepository) {
        this.bookRepository = bookRepository;
        this.borrowedBookRepository = borrowedBookRepository;
    }

    public int getTotalBookCount() {
        return (int) bookRepository.count();
    }

    public List<BorrowedBookdto> getBorrowedBooksByUserId(Long userId) {
        List<BorrowedBook> borrowedBooks = borrowedBookRepository.findByUserId(userId);
        LocalDate currentDate = LocalDate.now();
        int finePerDay = 10;

        return borrowedBooks.stream().map(b -> {
            BorrowedBookdto dto = new BorrowedBookdto();
            dto.setBookId(b.getBook().getId());
            dto.setTitle(b.getBook().getTitle());
            dto.setAuthor(b.getBook().getAuthor());
            dto.setBorrowedDate(b.getBorrowedDate().toLocalDate());
            dto.setDueDate(b.getDueDate() != null ? b.getDueDate().toLocalDate() : null);

            if (b.getDueDate() != null) {
                int overdueDays = Math.max(0,
                        (int) (currentDate.toEpochDay() - b.getDueDate().toLocalDate().toEpochDay()));
                dto.setOverdueDays(overdueDays);
                dto.setFine(overdueDays * finePerDay);
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
