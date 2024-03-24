package com.example.bookstore.service.impl;

import com.example.grpc.service.GreetingServiceGrpc;
import com.example.grpc.service.HelloRequest;
import com.example.grpc.service.HelloResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
    @Override
    public void greeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.println(request);
        HelloResponse response = HelloResponse.newBuilder()
                .setGreeting("Hello from server," + request.getName())
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }
}
