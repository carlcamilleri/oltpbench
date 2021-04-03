package com.oltpbenchmark.benchmarks.tpcc;

import com.oltpbenchmark.WorkloadConfiguration;

import java.sql.SQLException;

public class TPCCWorkerFactory {
    public static TPCCWorker createWorker(TPCCBenchmark benchmarkModule, int id,
                                          int terminalWarehouseID, int terminalDistrictLowerID,
                                          int terminalDistrictUpperID, int numWarehouses) throws SQLException {
        if (benchmarkModule.getWorkloadConfiguration().getBenchmarkName().equalsIgnoreCase("tpcc_thespis")) {
            return new TPCCWorkerThespis(benchmarkModule, id, terminalWarehouseID, terminalDistrictLowerID, terminalDistrictUpperID, numWarehouses);
        } else {
            return new TPCCWorker(benchmarkModule, id, terminalWarehouseID, terminalDistrictLowerID, terminalDistrictUpperID, numWarehouses);
        }
    }
}
