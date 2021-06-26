package com.oltpbenchmark.benchmarks.ycsb;

import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.tpcc.TPCCBenchmark;
import com.oltpbenchmark.benchmarks.tpcc.TPCCWorker;
import com.oltpbenchmark.benchmarks.tpcc.TPCCWorkerThespis;

import java.sql.SQLException;

public class YCSBWorkerFactory {
    public static Worker<YCSBBenchmark> createWorker(YCSBBenchmark benchmarkModule, int id,
                                                     int init_record_count) throws SQLException {
        if (benchmarkModule.getWorkloadConfiguration().getBenchmarkName().equalsIgnoreCase("ycsb_thespis")) {
            return new YCSBWorkerThespis(benchmarkModule, id, init_record_count);
        } else {
            return new YCSBWorker(benchmarkModule, id, init_record_count);
        }
    }
}
