package org.library.service;

import org.library.dto.BorrowedBookdto;
import org.library.dto.Dashboarddto;
import org.library.entity.BorrowedBook;
import org.library.entity.User;
import org.library.repository.BookRepository;
import org.library.repository.BorrowedBookRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final BookRepository bookRepository;
    private final BorrowedBookRepository borrowedBookRepository;

    public DashboardService(BookRepository bookRepository, BorrowedBookRepository borrowedBookRepository) {
        this.bookRepository = bookRepository;
        this.borrowedBookRepository = borrowedBookRepository;
    }

    public Dashboarddto getDashboardDataForUser(User user) {
        long totalBooks = bookRepository.count();
        long totalBorrowedBooks = borrowedBookRepository.countByUserId(user.getId());
        List<BorrowedBook> borrowedBooks = borrowedBookRepository.findByUserId(user.getId());

        List<BorrowedBookdto> borrowedBookDtos = borrowedBooks.stream().map(book -> {
            BorrowedBookdto dto = new BorrowedBookdto();
            dto.setBookId(book.getBook().getId());
            dto.setTitle(book.getBook().getTitle());
            dto.setAuthor(book.getBook().getAuthor());
            dto.setBorrowedDate(book.getBorrowedDate().toLocalDate());
            dto.setDueDate(book.getDueDate().toLocalDate());

            // Calculate overdueDays and fine
            LocalDate today = LocalDate.now();
            long overdueDays = ChronoUnit.DAYS.between(book.getDueDate().toLocalDate(), today);
            if (overdueDays > 0) {
                dto.setOverdueDays((int) overdueDays);
                dto.setFine((int) overdueDays * 10); // e.g., ₹10 per day fine
            } else {
                dto.setOverdueDays(0);
                dto.setFine(0);
            }

            return dto;
        }).collect(Collectors.toList());

        return new Dashboarddto(
                totalBooks,
                totalBorrowedBooks,
                borrowedBookDtos,
                user.getRole()
        );
    }
}