package wood.mike.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import wood.mike.greet.DelayedReply;
import wood.mike.greet.GreeterGrpc;
import wood.mike.greet.HelloReply;
import wood.mike.greet.HelloRequest;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class GreetGrpcClient {

    private static final String HOST = "localhost";
    private static final int PORT = 50051;

    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterStub nonBlockingStub;
    private final ManagedChannel channel;
    private final Scanner scanner;

    public GreetGrpcClient() {
        channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        blockingStub = GreeterGrpc.newBlockingStub(channel);
        nonBlockingStub = GreeterGrpc.newStub(channel);
        scanner = new Scanner(System.in);
    }

    private void run() throws InterruptedException {
        String result = switch (getChoice()) {
            case 1 -> unaryExample();
            case 2 -> serverStreamingExample();
            case 3 -> clientStreamingExample();
            case 4 -> biDirectionalExample();
            default -> throw new IllegalStateException("Unexpected value");
        };

        System.out.println(result);
        close();
    }

    private String unaryExample() {
        var helloReply = blockingStub.sayHello(HelloRequest.newBuilder().setName("Mike").setGreeting("Howdy").build());
        return String.format("Server reply: %s", helloReply);
    }

    private String serverStreamingExample() {
        Iterator<HelloReply> helloReplies = blockingStub.parrotSaysHello(HelloRequest.newBuilder().setName("Mike").setGreeting("Howdy").build());
        List<HelloReply> replies = new ArrayList<>();
        while (helloReplies.hasNext()) {
            replies.add(helloReplies.next());
        }
        return String.format("Server replies: %s", replies);
    }

    private String clientStreamingExample() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<DelayedReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(DelayedReply delayedReply) {
                System.out.println("Server reply: " + delayedReply.getMessage());
                delayedReply.getRequestList().forEach(req ->
                        System.out.println("Echoed: " + req.getGreeting() + " " + req.getName())
                );
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error from server: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server finished processing.");
                latch.countDown();
            }
        };
        StreamObserver<HelloRequest> requestObserver = nonBlockingStub.chattyClientSaysHello(responseObserver);

        Map.of("Sam", "Hello", "Kipper", "Bonjour", "Dorris", "Howdy")
                .entrySet()
                .stream()
                .map(e -> HelloRequest.newBuilder().setName(e.getKey()).setGreeting(e.getValue()).build())
                .forEach(requestObserver::onNext);

        requestObserver.onCompleted();

        latch.await(5, TimeUnit.SECONDS);
        return "Done";
    }

    private String biDirectionalExample() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<HelloReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(HelloReply helloReply) {
                System.out.printf("Server reply %s%n", helloReply.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
                System.out.println("Error from server: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Server finished processing.");
                latch.countDown();
            }
        };
        StreamObserver<HelloRequest> requestObserver = nonBlockingStub.interactingHello(responseObserver);

        Map.of("Mike", "Hello", "Dave", "Bonjour", "Chris", "Howdy")
                .entrySet()
                .stream()
                .map(e -> HelloRequest.newBuilder().setName(e.getKey()).setGreeting(e.getValue()).build())
                .forEach(requestObserver::onNext);

        Thread.sleep(500);

        requestObserver.onCompleted();

        return  "Done";
    }

    private void close() {
        scanner.close();
        channel.shutdown();
    }


    private int getChoice() {
        System.out.println("Enter a number between 1 and 4:");
        return scanner.nextInt();
    }

    public static void main(String[] args) throws InterruptedException {
        new GreetGrpcClient().run();
    }
}
