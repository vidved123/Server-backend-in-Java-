package org.library.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ReturnBooksdto {
    private Long bookId;
    private String title;
    private String author;
    private String borrowedDate;
    private String dueDate;
    private String username; // Only set if the user is an admin
    private Long userId; // This will hold the user ID for the book
}
