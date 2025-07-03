package org.library.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class BorrowedBookdto {

    // Getters and Setters
    private Long bookId;
    private String title;
    private String author;
    private LocalDate borrowedDate;
    private LocalDate dueDate;
    private String username;
    private int overdueDays;
    private double fine;

}