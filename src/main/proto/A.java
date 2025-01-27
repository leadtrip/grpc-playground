syntax = "proto3";
option java_multiple_files = true;
package com.baeldung.grpc;

message HelloRequest {
    string firstName = 1;
    string lastName = 2;
}

message HelloResponse {
    string greeting = 1;
}

service HelloService {
    rpc hello(HelloRequest) returns (HelloResponse);
}