package com.example.bookstore.mapper;

import com.example.bookstore.model.Book;
import com.example.grpc.service.BookCreateRequest;
import com.example.grpc.service.BookId;
import com.example.grpc.service.BookUpdateRequest;
import com.example.grpc.service.FullBookInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface BookMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "bookDetails.title", target = "title")
    @Mapping(source = "bookDetails.author", target = "author")
    @Mapping(source = "bookDetails.isbn", target = "isbn")
    @Mapping(source = "bookDetails.quantity", target = "quantity")
    Book bookCreateRequestToBook(BookCreateRequest bookCreateRequest);

    BookId bookToBookId(Book book);

    @Mapping(source = "id", target = "fullBookInfo.id")
    @Mapping(source = "title", target = "fullBookInfo.bookDetails.title")
    @Mapping(source = "author", target = "fullBookInfo.bookDetails.author")
    @Mapping(source = "isbn", target = "fullBookInfo.bookDetails.isbn")
    @Mapping(source = "quantity", target = "fullBookInfo.bookDetails.quantity")
    FullBookInfoResponse bookToFullBookInfoResponse(Book book);

    @Mapping(source = "fullBookInfo.id", target = "id")
    @Mapping(source = "fullBookInfo.bookDetails.title", target = "title")
    @Mapping(source = "fullBookInfo.bookDetails.author", target = "author")
    @Mapping(source = "fullBookInfo.bookDetails.isbn", target = "isbn")
    @Mapping(source = "fullBookInfo.bookDetails.quantity", target = "quantity")
    Book bookUpdateRequestToBook(BookUpdateRequest request);
}
