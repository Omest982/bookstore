package com.example.bookstore.service.impl;

import com.example.bookstore.exception.EntityNotFoundException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.grpc.service.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class BookServiceImpl extends BookServiceGrpc.BookServiceImplBase {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public void getBookById(BookId request, StreamObserver<FullBookInfoResponse> responseObserver) {
        UUID bookId = UUID.fromString(request.getId());
        Mono<Book> bookMono = getBookById(bookId);

        bookMono.subscribe(
                entity -> {
                    responseObserver.onNext(bookMapper.bookToFullBookInfoResponse(entity));

                    responseObserver.onCompleted();
                },
                error -> {
                    responseObserver.onError(
                            Status.NOT_FOUND
                                    .withDescription(error.getMessage())
                                    .withCause(error)
                                    .asRuntimeException()
                    );
                }
        );
    }

    @Override
    public void addBook(BookCreateRequest request, StreamObserver<BookId> responseObserver) {
        Book book = bookMapper.bookCreateRequestToBook(request);
        Mono<Book> bookMono = bookRepository.save(book);

        bookMono.subscribe(
                entity -> {
                    responseObserver.onNext(bookMapper.bookToBookId(entity));

                    responseObserver.onCompleted();
                },
                error ->{
                    responseObserver.onError(
                            Status.INTERNAL
                                    .withDescription(error.getMessage())
                                    .withCause(error)
                                    .asRuntimeException()
                    );
                }
        );
    }

    @Override
    public void updateBook(BookUpdateRequest request, StreamObserver<BookId> responseObserver) {
        UUID bookId = UUID.fromString(request.getFullBookInfo().getId());

        Mono<Book> bookMono = getBookById(bookId);

        bookMono.flatMap(
                entity -> {
                    Book updatedBook = bookMapper.bookUpdateRequestToBook(request);
                    updatedBook.setId(bookId);
                    return bookRepository.save(updatedBook);
                })
                .map(bookMapper::bookToBookId)
                .subscribe(
                        bookIdResponse -> {
                            responseObserver.onNext(bookIdResponse);
                            responseObserver.onCompleted();
                        },
                        error -> {
                            responseObserver.onError(
                                    Status.NOT_FOUND
                                            .withDescription(error.getMessage())
                                            .withCause(error)
                                            .asRuntimeException()
                            );
                        }
                );
    }

    @Override
    public void deleteBookById(BookId request, StreamObserver<DeleteResponse> responseObserver) {
        Mono<Book> bookMono = getBookById(UUID.fromString(request.getId()));

        bookMono.flatMap(
                entity -> bookRepository.delete(entity)
                            .then(Mono.just(DeleteResponse.newBuilder().setResponse(true).build()))
                )
                .subscribe(
                        response -> {
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        error -> {
                            responseObserver.onError(
                                    Status.NOT_FOUND
                                            .withDescription(error.getMessage())
                                            .withCause(error)
                                            .asRuntimeException()
                            );
                        }
                );
    }

    private Mono<Book> getBookById(UUID bookId){
        return bookRepository.findById(bookId)
                .switchIfEmpty(
                        Mono.error(new EntityNotFoundException(String.format("Book with id %s not found!", bookId))));
    }
}
