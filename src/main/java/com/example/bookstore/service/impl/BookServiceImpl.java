package com.example.bookstore.service.impl;

import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.grpc.service.*;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class BookServiceImpl extends BookServiceGrpc.BookServiceImplBase {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public void getBookById(BookId request, StreamObserver<FullBookInfoResponse> responseObserver) {
        UUID bookId = UUID.fromString(request.getId());
        Book book = getBookById(bookId);

        responseObserver.onNext(bookMapper.bookToFullBookInfoResponse(book));

        responseObserver.onCompleted();
    }

    @Override
    public void addBook(BookCreateRequest request, StreamObserver<BookId> responseObserver) {
        Book book = bookMapper.bookCreateRequestToBook(request);

        responseObserver.onNext(bookMapper.bookToBookId(bookRepository.save(book)));

        responseObserver.onCompleted();
    }

    @Override
    public void updateBook(BookUpdateRequest request, StreamObserver<BookId> responseObserver) {
        UUID bookId = UUID.fromString(request.getFullBookInfo().getId());

        Book book = getBookById(bookId);

        book = bookMapper.bookUpdateRequestToBook(request);

        responseObserver.onNext(bookMapper.bookToBookId(bookRepository.save(book)));

        responseObserver.onCompleted();
    }

    @Override
    public void deleteBookById(BookId request, StreamObserver<DeleteResponse> responseObserver) {
        Book book = getBookById(UUID.fromString(request.getId()));

        bookRepository.delete(book);

        responseObserver.onNext(DeleteResponse.newBuilder().setResponse(true).build());

        responseObserver.onCompleted();
    }

    private Book getBookById(UUID bookId){
        return bookRepository.findById(bookId).orElseThrow(()
                -> new EntityNotFoundException(String.format("Book with id %s not found!", bookId)));
    }
}
