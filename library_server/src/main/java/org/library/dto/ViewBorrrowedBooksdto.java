package org.library.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ViewBorrrowedBooksdto {
    private Long bookId;
    private String title;
    private String author;
    private String borrowedDate; // formatted as String
    private String dueDate;
    private String username; // Only for admin view
}
