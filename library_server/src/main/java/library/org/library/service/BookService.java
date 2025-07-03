package org.library.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.library.entity.Book;
import org.library.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @SuppressWarnings("null")
    public void addBook(Book book, MultipartFile imageFile) throws Exception {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Use the original filename for the image
                String imageName = imageFile.getOriginalFilename();

                // Sanitize the filename (optional)
                assert imageName != null;
                imageName = imageName.replaceAll("[^a-zA-Z0-9._-]", "_");

                // Define the path to store the image (inside the static/images folder)
                File imagePath = new File("src/main/resources/static/images", imageName);

                // Ensure the directory exists, or create it
                File directory = new File(imagePath.getParent());
                if (!directory.exists()) {
                    boolean dirsCreated = directory.mkdirs();
                    if (!dirsCreated) {
                        throw new Exception("Failed to create directories for saving the image.");
                    }
                }

                // Transfer the image file to the specified path
                imageFile.transferTo(imagePath);

                // Set the image name to store in the book record
                book.setImage(imageName);
                book.setImageUrl("/images/" + imageName);  // Correct URL for static content
            } catch (Exception e) {
                throw new Exception("Error while saving image file: " + e.getMessage(), e);
            }
        }
        book.setAvailable(true);
        bookRepository.save(book);
    }

    public List<Book> getBooks(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return bookRepository.findAll();
        }

        // Try parsing the searchQuery to see if it is a valid book ID
        try {
            Long id = Long.valueOf(searchQuery.trim());
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrId(searchQuery, searchQuery, id);
        } catch (NumberFormatException e) {
            // If it's not a number, search by title or author
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(searchQuery, searchQuery);
        }
    }

    public void importFromExcel(MultipartFile excelFile) throws Exception {
        try (InputStream inputStream = excelFile.getInputStream()) {
            try (// Create a Workbook from the InputStream
            Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

                // Iterate through the rows in the sheet (skip the header row)
                for (Row row : sheet) {
                    // Skip the header row (assumed to be the first row)
                    if (row.getRowNum() == 0) {
                        continue;
                    }

                    // Retrieve each cell value from the row
                    Cell titleCell = row.getCell(1);      // Column 1 = title
                    Cell authorCell = row.getCell(2);     // Column 2 = author
                    Cell totalCopiesCell = row.getCell(6); // Column 6 = totalCopies

                    // Skip rows that don't have complete data
                    if (titleCell == null || authorCell == null || totalCopiesCell == null) {
                        continue;
                    }

                    String title = titleCell.getStringCellValue().trim();
                    String author = authorCell.getStringCellValue().trim();
                    int totalCopies = (int) totalCopiesCell.getNumericCellValue(); // Assuming totalCopies is a number

                    // Skip rows where title is a number (e.g., junk rows like "1.0")
                    if (title.matches("\\d+(\\.0)?")) {
                        continue;
                    }

                    // Create a new book entity and populate it
                    Book book = getBook(title, author, totalCopies);

                    // Save the book in the repository
                    bookRepository.save(book);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error while reading the Excel file: " + e.getMessage(), e);
        }
    }

    @NotNull
    private static Book getBook(String title, String author, int totalCopies) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies); // Set available copies equal to total copies initially
        book.setAvailable(true);  // Mark as available
        book.setImage("default.jpg");  // Default image if no image is uploaded
        book.setImageUrl("/static/images/default.jpg");  // Default image URL
        return book;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooks(String searchQuery) {
        try {
            // Try parsing the searchQuery to a Long to check if it's a book ID
            Long id = Long.valueOf(searchQuery.trim());

            // If it's a valid number, search by title, author, or book ID
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrId(
                    searchQuery, searchQuery, id
            );
        } catch (NumberFormatException e) {
            // If it's not a number, just search by title or author
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                    searchQuery, searchQuery
            );
        }
    }

    @Transactional
    public void deleteBooks(List<Long> bookIds) throws Exception {
        for (Long bookId : bookIds) {
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book != null) {
                logger.info("Deleting book: {}", book.getTitle()); // Logging the book title being deleted
                bookRepository.delete(book);
            } else {
                throw new Exception("Book with ID " + bookId + " not found");
            }
        }
    }

    // New method to check if a book is available
    public boolean isBookAvailable(Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            return book.getAvailableCopies() > 0;  // Check if there are available copies
        }
        return false;  // If the book doesn't exist, return false
    }
}