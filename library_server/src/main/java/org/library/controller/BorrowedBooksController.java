package org.library.controller;

import org.library.dto.BorrowedBookdto;
import org.library.entity.Book;
import org.library.service.BorrowedBookService;
import org.library.service.BookService;
import org.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/borrow")
public class BorrowedBooksController {

    @Autowired
    private BorrowedBookService borrowBookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    // GET method for showing the borrow page
    @GetMapping
    public String getBorrowPage(Authentication authentication, Model model,
                                @RequestParam(required = false) String search) {
        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("error", "User is not logged in");
            return "error";
        }

        String username = authentication.getName();
        Long userId = userService.getUserIdByUsername(username);

        prepareBorrowPage(model, userId, username, search, null);

        return "borrow_books";
    }

    // POST method for borrowing books
    @PostMapping
    public String borrowBooks(@RequestParam(required = false) List<Long> bookIds,
                              @RequestParam(required = false, name = "user_id") Long userId,
                              Authentication authentication,
                              Model model) {

        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("error", "User is not logged in");
            return "error";
        }

        String username = authentication.getName();
        Long authenticatedUserId = userService.getUserIdByUsername(username);
        Long actualUserId = (userService.isAdmin(username) && userId != null) ? userId : authenticatedUserId;

        if (!userService.existsById(actualUserId)) {
            prepareBorrowPage(model, authenticatedUserId, username, null, "Invalid user ID.");
            return "borrow_books";
        }

        if (bookIds == null || bookIds.isEmpty()) {
            prepareBorrowPage(model, actualUserId, username, null, "No books selected for borrowing.");
            return "borrow_books";
        }

        for (Long bookId : bookIds) {
            if (!bookService.isBookAvailable(bookId)) {
                prepareBorrowPage(model, actualUserId, username, null, "Book with ID " + bookId + " is not available.");
                return "borrow_books";
            }
        }

        borrowBookService.borrowBooksForUser(bookIds, actualUserId);

        prepareBorrowPage(model, actualUserId, username, null, "Books borrowed successfully!");
        return "borrow_books";
    }

    // AJAX: Verify user ID
    @PostMapping("/verify-user-id")
    @ResponseBody
    public ResponseEntity<Boolean> verifyUserId(@RequestParam Long userId) {
        boolean exists = userService.existsById(userId);
        return ResponseEntity.ok(exists);
    }

    // DRY helper method
    private void prepareBorrowPage(Model model, Long userId, String username, String searchQuery, String message) {
        List<BorrowedBookdto> borrowedBooks = borrowBookService.getUserBorrowedBooks(userId);
        List<Book> books = bookService.getBooks(searchQuery);

        model.addAttribute("borrowedBooks", borrowedBooks);
        model.addAttribute("books", books);
        model.addAttribute("searchQuery", searchQuery);

        if (message != null) {
            model.addAttribute("message", message);
        }

        if (userService.isAdmin(username)) {
            model.addAttribute("isAdmin", true);
        }
    }
}
