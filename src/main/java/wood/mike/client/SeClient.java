package wood.mike.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import wood.mike.se.SeSaleRangeRequest;
import wood.mike.se.SeSaleReply;
import wood.mike.se.SeSaleRequest;
import wood.mike.se.SecretEscapesGrpc;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class SeClient {

    private static final String HOST = "localhost";
    private static final int PORT = 50052;

    private final SecretEscapesGrpc.SecretEscapesBlockingStub blockingStub;
    private final SecretEscapesGrpc.SecretEscapesStub nonBlockingStub;
    private final ManagedChannel channel;
    private final Scanner scanner;

    public SeClient() {
        channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        blockingStub = SecretEscapesGrpc.newBlockingStub(channel);
        nonBlockingStub = SecretEscapesGrpc.newStub(channel);
        scanner = new Scanner(System.in);
    }

    private void run() throws InterruptedException {
        String result = switch (getChoice()) {
            case 1 -> unaryExample();
            case 2 -> serverStreamingExample();
            //case 3 -> clientStreamingExample();
            //case 4 -> biDirectionalExample();
            default -> throw new IllegalStateException("Unexpected value");
        };

        System.out.println(result);
        close();
    }

    private String unaryExample() {
        var seResponse = blockingStub.getSeSale(SeSaleRequest.newBuilder().setId("A1234").build());
        return String.format("Server reply: %s", seResponse);
    }

    private String serverStreamingExample() {
        Iterator<SeSaleReply> seSaleReplyIterator =
                blockingStub.getAllSeSales(SeSaleRangeRequest
                        .newBuilder()
                        .setStart("2025-02-01")
                        .setEnd("2025-02-07")
                        .build());

        List<SeSaleReply> list = Stream.generate(() -> null)
                .takeWhile(x -> seSaleReplyIterator.hasNext())
                .map(n -> seSaleReplyIterator.next())
                .peek(System.out::println)
                .toList();

        return "Done";
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
        new SeClient().run();
    }
}
