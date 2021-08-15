package com.oltpbenchmark.benchmarks.ycsb.thespis;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ThespisClient {

    private static  ManagedChannel _channel = ManagedChannelBuilder.forAddress("10.132.15.193", 30004)
            .usePlaintext()
            .build();

    private static   QueryServiceGrpc.QueryServiceBlockingStub _stub
            = QueryServiceGrpc.newBlockingStub(_channel);


    public static Services.SelectYCSBResponse select(int id){



        return _stub.selectYCSB(Services.SelectYCSBRequest.newBuilder()
                .setId(id)
                .build());

    }
}
