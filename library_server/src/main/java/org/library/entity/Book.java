package org.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private boolean available;
    private String image;  // Stores image filename or path
    private String imageUrl; // New field to store image URL (optional)

    private int availableCopies;
    private int totalCopies;
}
