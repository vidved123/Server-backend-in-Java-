package org.library.controller;

import java.io.File;
import java.util.List;

import org.library.dto.BorrowedBookdto;
import org.library.entity.Book;
import org.library.service.BookService;
import org.library.service.BorrowedBookService;
import org.library.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/borrow")
public class BorrowedBooksController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowedBooksController.class);

    @Autowired
    private BorrowedBookService borrowBookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Value("${app.image.folder}")
    private String imageFolderPath;

    public BorrowedBooksController(BorrowedBookService borrowBookService) {
        this.borrowBookService = borrowBookService;
    }

    // GET method for showing the borrow page
    @GetMapping
    public String getBorrowPage(Authentication authentication, Model model,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false, name = "user_id") Long userId,
                                @RequestParam(required = false, name = "selectedBookIds") String selectedBookIds,
                                @RequestParam(required = false) String verify,
                                HttpSession session) {
        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("error", "User is not logged in");
            return "redirect:/login";
        }

        String username = authentication.getName();
        Long authenticatedUserId = userService.getUserIdByUsername(username);
        boolean isVerified = false;
        String verificationMessage = null;

        // For admins, use session to persist verified user ID
        if (userService.isAdmin(username)) {
            if (verify != null && userId != null) {
                // Verification attempt
                if (userService.existsById(userId)) {
                    session.setAttribute("verifiedUserId", userId);
                    session.setAttribute("isVerified", true);
                    isVerified = true;
                    verificationMessage = "✅ User verified successfully!";
                } else {
                    session.removeAttribute("verifiedUserId");
                    session.setAttribute("isVerified", false);
                    isVerified = false;
                    verificationMessage = "❌ User not found. Please check the User ID.";
                }
            } else {
                // Not a verification attempt, check session
                Long sessionUserId = (Long) session.getAttribute("verifiedUserId");
                Boolean sessionVerified = (Boolean) session.getAttribute("isVerified");
                if (sessionUserId != null && Boolean.TRUE.equals(sessionVerified)) {
                    userId = sessionUserId;
                    isVerified = true;
                } else {
                    isVerified = false;
                }
            }
            model.addAttribute("verifiedUserId", userId != null ? userId : "");
            model.addAttribute("isVerified", isVerified);
            model.addAttribute("verificationMessage", verificationMessage);
            if (isVerified && userId != null) {
                prepareBorrowPage(model, userId, username, search, null);
            } else {
                prepareBorrowPage(model, authenticatedUserId, username, search, null);
            }
        } else {
            model.addAttribute("verifiedUserId", "");
            model.addAttribute("isVerified", true);
            prepareBorrowPage(model, authenticatedUserId, username, search, null);
        }

        // Pass selectedBookIds to the model for JS to re-check checkboxes
        if (selectedBookIds != null) {
            model.addAttribute("selectedBookIds", selectedBookIds);
        } else {
            model.addAttribute("selectedBookIds", "");
        }

        return "borrow_books";
    }

    // POST method for borrowing books
    @PostMapping
    public String borrowBooks(@RequestParam(required = false) List<Long> bookIds,
                              @RequestParam(required = false, name = "user_id") Long userId,
                              Authentication authentication,
                              Model model,
                              HttpSession session) {

        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("error", "User is not logged in");
            return "redirect:/login";
        }

        String username = authentication.getName();
        Long authenticatedUserId = userService.getUserIdByUsername(username);
        Long actualUserId;
        if (userService.isAdmin(username)) {
            // Use session for verified user ID
            if (userId != null) {
                session.setAttribute("verifiedUserId", userId);
            }
            Long sessionUserId = (Long) session.getAttribute("verifiedUserId");
            if (sessionUserId == null || !userService.existsById(sessionUserId)) {
                prepareBorrowPage(model, authenticatedUserId, username, null, "❌ Invalid user ID. Please verify the user ID before borrowing books.");
                model.addAttribute("verifiedUserId", "");
                return "borrow_books";
            }
            actualUserId = sessionUserId;
            model.addAttribute("verifiedUserId", sessionUserId);
            logger.info("Admin {} is borrowing books for user ID: {}", username, actualUserId);
        } else {
            actualUserId = authenticatedUserId;
            model.addAttribute("verifiedUserId", "");
            logger.info("User {} is borrowing books for themselves (ID: {})", username, actualUserId);
        }

        if (bookIds == null || bookIds.isEmpty()) {
            prepareBorrowPage(model, actualUserId, username, null, "⚠️ Please select at least one book to borrow.");
            return "borrow_books";
        }

        // Check borrow limit (3 books per session)
        if (bookIds.size() > 3) {
            prepareBorrowPage(model, actualUserId, username, null, "❌ You can only borrow up to 3 books at a time. Please select fewer books.");
            return "borrow_books";
        }

        for (Long bookId : bookIds) {
            if (!bookService.isBookAvailable(bookId)) {
                prepareBorrowPage(model, actualUserId, username, null, "❌ Book with ID " + bookId + " is not available for borrowing.");
                return "borrow_books";
            }
        }

        borrowBookService.borrowBooksForUser(bookIds, actualUserId);

        // Create appropriate success message based on who the books are borrowed for
        String successMessage;
        if (userService.isAdmin(username) && !actualUserId.equals(authenticatedUserId)) {
            // Admin borrowing for another user
            successMessage = "✅ Books borrowed successfully for User ID: " + actualUserId + "! Check the user's borrowed books list.";
        } else {
            // Regular user or admin borrowing for themselves
            successMessage = "✅ Books borrowed successfully! Check your borrowed books list.";
        }

        prepareBorrowPage(model, actualUserId, username, null, successMessage);
        return "borrow_books";
    }

    // AJAX: Verify user ID
    @PostMapping("/verify-user-id")
    @ResponseBody
    public ResponseEntity<Boolean> verifyUserId(@RequestParam("userId") Long userId) {
        logger.info("Verifying user ID: {}", userId);
        boolean exists = userService.existsById(userId);
        logger.info("User ID {} exists: {}", userId, exists);
        return ResponseEntity.ok(exists);
    }

    // DRY helper method
    private void prepareBorrowPage(Model model, Long userId, String username, String searchQuery, String message) {
        List<BorrowedBookdto> borrowedBooks = borrowBookService.getUserBorrowedBooks(userId);
        List<Book> books = bookService.getBooks(searchQuery);

        // Set default image if file does not exist for books list
        for (Book book : books) {
            logger.info("Checking image for book '{}': {}", book.getTitle(), book.getImage());
            if (book.getImage() == null || book.getImage().isEmpty()) {
                book.setImage("default.jpg");
            } else {
                File imageFile = new File(imageFolderPath + "/" + book.getImage());
                logger.info("Image file exists: {} -> {}", imageFile.getPath(), imageFile.exists());
                if (!imageFile.exists()) {
                    book.setImage("default.jpg");
                }
            }
        }

        // Set default image if file does not exist for borrowedBooks list
        for (BorrowedBookdto dto : borrowedBooks) {
            String image = dto.getImage();
            if (image == null || image.isEmpty()) {
                dto.setImage("default.jpg");
            } else {
                File imageFile = new File(imageFolderPath + "/" + image);
                if (!imageFile.exists()) {
                    dto.setImage("default.jpg");
                }
            }
        }

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
