package org.library.exception;

public class BookNotFoundException extends RuntimeException {

    // Constructor that accepts a message
    public BookNotFoundException(String message) {
        super(message);
    }
}
