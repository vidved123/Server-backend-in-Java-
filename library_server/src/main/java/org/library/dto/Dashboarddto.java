package org.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.library.entity.enums.Role;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dashboarddto {
    private long totalBooks;
    private long totalBorrowedBooks;  // Update this to a `long` type to hold the count
    private List<BorrowedBookdto> borrowedBooks;
    private Role userRole;
}
