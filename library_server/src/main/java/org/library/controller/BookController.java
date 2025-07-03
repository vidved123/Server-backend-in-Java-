package org.library.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.library.entity.Book;
import org.library.service.BookMasterService;
import org.library.service.BookService;
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

    private final BookService bookService;
    private final BookMasterService bookMasterService;

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
        model.addAttribute("messages", new ArrayList<>());
        return "add_books";
    }

    @PostMapping("/add_books")
    public String handleAddBook(@ModelAttribute Book book,
                                @RequestParam("image") MultipartFile image,
                                Model model) {
        try {
            if (image.isEmpty()) {
                addMessage(model, "error", "Image file is required.");
                return "add_books";
            }

            bookService.addBook(book, image);
            addMessage(model, "success", "Book added successfully!");
            return "redirect:/book_master";
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

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "book_master";
    }

    @GetMapping("/view_books")
    public String viewBooks(@RequestParam(required = false) String search, Model model) {
        List<Book> books = (search != null && !search.trim().isEmpty()) ?
                bookService.searchBooks(search) : bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("searchQuery", search);

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("messages", new ArrayList<>());
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