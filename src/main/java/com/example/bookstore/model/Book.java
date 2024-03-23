package com.example.bookstore.model;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Book {
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private int quantity;
}
