package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends R2dbcRepository<Book, UUID> {
}
