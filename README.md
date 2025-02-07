### setup
This will generate the GRPC code from the proto files

`mvn clean install`

### running
There's a server and client for the basic Hello example, start HelloServer first then HelloClient to get a single reply.

There's also a GreetClient and SeClient for servers that exist in this [python](https://github.com/leadtrip/python-grpc-playground) project.

Follow the instructions in the python project to start the relevant server then start the associated client in this Java project.