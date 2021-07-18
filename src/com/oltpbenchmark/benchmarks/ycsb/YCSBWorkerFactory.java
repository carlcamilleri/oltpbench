package com.oltpbenchmark.benchmarks.ycsb;

import com.oltpbenchmark.api.Worker;

import java.sql.SQLException;

public class YCSBWorkerFactory {
    public static Worker<YCSBBenchmark> createWorker(YCSBBenchmark benchmarkModule, int id,
                                                     int init_record_count) throws SQLException {
        if (benchmarkModule.getWorkloadConfiguration().getBenchmarkName().equalsIgnoreCase("ycsb_thespis")) {
            var thespisUrl = benchmarkModule.getWorkloadConfiguration().getXmlConfig().getDocument().getElementsByTagName("thespisUrl").item(0).getTextContent();
            return new YCSBWorkerThespis(benchmarkModule, id, init_record_count,thespisUrl);
        } else {
            return new YCSBWorker(benchmarkModule, id, init_record_count);
        }
    }
}
