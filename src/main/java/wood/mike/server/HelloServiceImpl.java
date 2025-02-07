package wood.mike.server;

import io.grpc.stub.StreamObserver;
import wood.mike.hello.HelloRequest;
import wood.mike.hello.HelloResponse;
import wood.mike.hello.HelloServiceGrpc;

public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase
{
    @Override
    public void hello(
        HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

        System.out.println("Building response");

        String greeting = "Hello " +
                request.getFirstName() +
                " " +
                request.getLastName();

        HelloResponse response = HelloResponse.newBuilder()
                .setGreeting(greeting)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}