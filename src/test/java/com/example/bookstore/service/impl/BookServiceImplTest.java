package com.example.bookstore.service.impl;

import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.grpc.service.*;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void getBookById_Success() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        BookId request = BookId.newBuilder().setId(uuid.toString()).build();

        Book book = Book.builder()
                .id(uuid)
                .title("Test")
                .author("Test")
                .build();

        BookDetails bookDetails = BookDetails.newBuilder()
                .setTitle(book.getTitle())
                .setAuthor(book.getAuthor())
                .build();

        FullBookInfo fullBookInfo = FullBookInfo.newBuilder()
                .setId(uuid.toString())
                .setBookDetails(bookDetails)
                .build();

        FullBookInfoResponse expectedResponse = FullBookInfoResponse.newBuilder()
                .setFullBookInfo(fullBookInfo)
                .build();

        when(bookRepository.findById(uuid)).thenReturn(Mono.just(book));
        when(bookMapper.bookToFullBookInfoResponse(any(Book.class))).thenReturn(expectedResponse);

        StreamRecorder<FullBookInfoResponse> responseObserver = StreamRecorder.create();

        // Act
        bookService.getBookById(request, responseObserver);

        // Assert
        assertFalse(responseObserver.getValues().isEmpty());
        assertEquals(expectedResponse, responseObserver.getValues().get(0));
        assertNull(responseObserver.getError());
    }

    @Test
    void addBook_Success() {
        // Arrange
        UUID uuid = UUID.randomUUID();

        BookDetails bookDetails = BookDetails.newBuilder()
                .setAuthor("Test")
                .setTitle("Test")
                .build();

        BookCreateRequest request = BookCreateRequest.newBuilder()
                .setBookDetails(bookDetails)
                .build();

        Book transientBook = Book.builder()
                .title(request.getBookDetails().getTitle())
                .author(request.getBookDetails().getAuthor())
                .build();

        Book persistentBook = Book.builder()
                .id(uuid)
                .title(request.getBookDetails().getTitle())
                .author(request.getBookDetails().getAuthor())
                .build();

        BookId expectedBookId = BookId.newBuilder()
                .setId(uuid.toString())
                .build();

        when(bookMapper.bookCreateRequestToBook(any(BookCreateRequest.class))).thenReturn(transientBook);
        when(bookRepository.save(transientBook)).thenReturn(Mono.just(persistentBook));
        when(bookMapper.bookToBookId(any(Book.class))).thenReturn(expectedBookId);

        StreamRecorder<BookId> responseObserver = StreamRecorder.create();

        // Act
        bookService.addBook(request, responseObserver);

        // Assert
        assertFalse(responseObserver.getValues().isEmpty());
        assertEquals(expectedBookId, responseObserver.getValues().get(0));
        assertNull(responseObserver.getError());
    }
}
