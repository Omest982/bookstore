package com.example.bookstore.service.impl;

import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.grpc.service.BookCreateRequest;
import com.example.grpc.service.BookDetails;
import com.example.grpc.service.BookId;
import com.example.grpc.service.BookServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class BookServiceImplIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookMapper bookMapper;

    private ManagedChannel channel;

    private Server inProcessServer;

    @Container
    private static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:16.1");

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.url", postgresqlContainer:: getJdbcUrl);
        registry.add("spring.liquibase.user", postgresqlContainer::getUsername);
        registry.add("spring.liquibase.password", postgresqlContainer::getPassword);

        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + postgresqlContainer.getHost() + ":" + postgresqlContainer.getFirstMappedPort()
                + "/" + postgresqlContainer.getDatabaseName());
        registry.add("spring.r2dbc.username", postgresqlContainer::getUsername);
        registry.add("spring.r2dbc.password", postgresqlContainer::getPassword);
    }

    @BeforeEach
    public void setup() throws Exception {
        // Configure your service with the real bookRepository and bookMapper
        BookServiceImpl bookService = new BookServiceImpl(bookRepository, bookMapper);

        String serverName = InProcessServerBuilder.generateName();
        inProcessServer = InProcessServerBuilder
                .forName(serverName).directExecutor().addService(bookService).build().start();

        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @AfterEach
    public void cleanup() throws Exception {
        if (channel != null) {
            channel.shutdownNow();
            channel.awaitTermination(3, TimeUnit.SECONDS);
        }
        // If you start the server, also shut it down here
        if (inProcessServer != null) {
            inProcessServer.shutdownNow();
            inProcessServer.awaitTermination(3, TimeUnit.SECONDS);
        }
    }

    @Test
    public void getBookById_whenNotFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        BookServiceGrpc.BookServiceBlockingStub stub = BookServiceGrpc.newBlockingStub(channel);

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                stub.getBookById(BookId.newBuilder().setId(id.toString()).build())
        );

        assertEquals(Status.NOT_FOUND.getCode(), Status.fromThrowable(exception).getCode());
    }

    @Test
    public void addBook_whenCreated_returnBookId() {
        BookServiceGrpc.BookServiceBlockingStub stub = BookServiceGrpc.newBlockingStub(channel);

        BookDetails bookDetails = BookDetails.newBuilder()
                .setTitle("Test")
                .setAuthor("Test")
                .setIsbn("Test")
                .build();

        BookCreateRequest bookCreateRequest = BookCreateRequest.newBuilder()
                .setBookDetails(bookDetails)
                .build();

        BookId bookId = stub.addBook(bookCreateRequest);

        assertNotNull(bookId);
    }

}
