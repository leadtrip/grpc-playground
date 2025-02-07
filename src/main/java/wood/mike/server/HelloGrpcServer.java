package wood.mike.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class HelloGrpcServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder
                .forPort(10980)
                .addService(new HelloServiceImpl()).build();

        server.start();
        server.awaitTermination();
    }
}
