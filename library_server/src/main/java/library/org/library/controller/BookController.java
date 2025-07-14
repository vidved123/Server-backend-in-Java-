package org.library.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.library.entity.Book;
import org.library.service.BookMasterService;
import org.library.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final BookMasterService bookMasterService;

    // Use the specific path for view books page
    private final String imageFolderPath = "/Users/vidhyut/Desktop/Prathisthan Projects/Server-Backend-java/demo/src/main/resources/static/images";

    public BookController(BookService bookService, BookMasterService bookMasterService) {
        this.bookService = bookService;
        this.bookMasterService = bookMasterService;
    }

    @SuppressWarnings("unchecked")
    private void addMessage(Model model, String category, String message) {
        Object messagesAttr = model.getAttribute("messages");
        List<Map<String, String>> messages;

        if (messagesAttr instanceof List<?>) {
            messages = (List<Map<String, String>>) messagesAttr;
        } else {
            messages = new ArrayList<>();
            model.addAttribute("messages", messages);
        }

        messages.add(Map.of("category", category, "text", message));
    }

    @GetMapping("/add_books")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("messages", new ArrayList<>());
        return "add_books";
    }

    @PostMapping("/add_books")
    public String handleAddBook(@ModelAttribute Book book,
                                @RequestParam("bookImgTestForce") MultipartFile image,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            // Set availableCopies to totalCopies on creation
            book.setAvailableCopies(book.getTotalCopies());

            // Log for debugging
            System.out.println("Book received: " + book);
            System.out.println("totalCopies: " + book.getTotalCopies());
            System.out.println("availableCopies: " + book.getAvailableCopies());

            if (image.isEmpty()) {
                addMessage(model, "error", "Image file is required.");
                return "add_books";
            }

            bookService.addBook(book, image);
            addMessage(model, "success", "Book added successfully!");
            return "add_books";
        } catch (Exception e) {
            addMessage(model, "error", "Error: " + e.getMessage());
            return "add_books";
        }
    }

    @PostMapping("/upload_books_excel")
    public String handleExcelUpload(@RequestParam("excel_file") MultipartFile file, Model model) {
        try {
            String filename = file.getOriginalFilename();
            if (file.isEmpty() || filename == null ||
                    (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
                addMessage(model, "error", "Please upload a valid Excel file.");
                return "add_books";
            }

            bookService.importFromExcel(file);
            addMessage(model, "success", "Books imported from Excel successfully!");
            return "redirect:/book_master";
        } catch (Exception e) {
            addMessage(model, "error", "Error importing Excel: " + e.getMessage());
            return "add_books";
        }
    }

    @GetMapping("/book_master")
    public String viewBooks(Model model) {
        List<Book> books = bookMasterService.getBooksMaster();
        model.addAttribute("books", books);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities() != null &&
                authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "book_master";
    }

    @GetMapping("/view_books")
    public String viewBooks(@RequestParam(required = false) String search, Model model) {
        List<Book> books = (search != null && !search.trim().isEmpty()) ?
                bookService.searchBooks(search) : bookService.getAllBooks();

        // Set default image if file does not exist
        for (Book book : books) {
            logger.info("Checking image for book '{}': {}", book.getTitle(), book.getImage());
            if (book.getImage() == null || book.getImage().isEmpty()) {
                book.setImage("default.jpg");
                logger.info("Book '{}' has no image, setting to default.jpg", book.getTitle());
            } else {
                // Use the configured image folder path from application.properties
                File imageFile = new File(imageFolderPath + "/" + book.getImage());
                logger.info("Image file path: {} -> exists: {}", imageFile.getPath(), imageFile.exists());
                if (!imageFile.exists()) {
                    book.setImage("default.jpg");
                    logger.info("Book '{}' image not found, setting to default.jpg", book.getTitle());
                }
            }
        }

        model.addAttribute("books", books);
        model.addAttribute("searchQuery", search);

        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities() != null &&
                authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("messages", new java.util.ArrayList<>());
        return "view_books";
    }

    @PostMapping("/delete_books")
    public String deleteBooks(@RequestParam("book_ids") List<Long> bookIds, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBooks(bookIds);
            redirectAttributes.addFlashAttribute("messages", List.of(Map.of(
                    "category", "success",
                    "text", "Books deleted successfully!"
            )));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messages", List.of(Map.of(
                    "category", "error",
                    "text", "Error deleting books: " + e.getMessage()
            )));
        }
        return "redirect:/view_books";
    }
}
