package wood.mike.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import wood.mike.hello.HelloRequest;
import wood.mike.hello.HelloResponse;
import wood.mike.hello.HelloServiceGrpc;

public class HelloClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 10980)
                .usePlaintext()
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub
                = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
                .setFirstName("Mike")
                .setLastName("Wood")
                .build());

        System.out.println("response: " + helloResponse);

        channel.shutdown();
    }
}
