/******************************************************************************
 *  Copyright 2015 by OLTPBenchmark Project                                   *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *    http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 ******************************************************************************/

package com.oltpbenchmark.benchmarks.ycsb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oltpbenchmark.DBWorkload;
import com.oltpbenchmark.WorkloadConfiguration;
import com.oltpbenchmark.api.BenchmarkModule;
import com.oltpbenchmark.api.Loader;
import com.oltpbenchmark.api.Worker;
import com.oltpbenchmark.benchmarks.ycsb.procedures.InsertRecord;
import com.oltpbenchmark.catalog.Table;
import com.oltpbenchmark.util.SQLUtil;
import org.apache.log4j.Logger;

public class YCSBBenchmark extends BenchmarkModule {
    private static final Logger LOG = Logger.getLogger(YCSBBenchmark.class);
    public YCSBBenchmark(WorkloadConfiguration workConf) {
        super("ycsb", workConf, true);
    }

    @Override
    protected List<Worker<? extends BenchmarkModule>> makeWorkersImpl(boolean verbose) throws IOException {
        List<Worker<? extends BenchmarkModule>> workers = new ArrayList<Worker<? extends BenchmarkModule>>();
        try {
            Connection metaConn = this.makeConnection();

            // LOADING FROM THE DATABASE IMPORTANT INFORMATION
            // LIST OF USERS

            Table t = this.catalog.getTable("USERTABLE");
            assert (t != null) : "Invalid table name '" + t + "' " + this.catalog.getTables();
            String userCount = SQLUtil.getMaxColSQL(this.workConf.getDBType(), t, "ycsb_key");
            Statement stmt = metaConn.createStatement();
            ResultSet res = stmt.executeQuery(userCount);
            int init_record_count = 0;
            while (res.next()) {
                init_record_count = res.getInt(1);
            }
            assert init_record_count > 0;
            res.close();



            var futGetTerminals = new ArrayList<CompletableFuture<Worker<YCSBBenchmark>>>(workConf.getTerminals());
            for (int i = 0; i < workConf.getTerminals(); ++i) {
                //LOG.info(String.format("Launching termnal %s ", i));
                var curI = i;
                var initRecordCount = init_record_count+1;
                futGetTerminals.add(
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return YCSBWorkerFactory.createWorker(this, curI, initRecordCount);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                            return null;
                        }));
                futGetTerminals.get(i).join();


            } // FOR
            var resFutures = Stream.of(futGetTerminals.toArray())
                    .map(f->((CompletableFuture<Worker<YCSBBenchmark>>)f))
                    .map(CompletableFuture::join).collect(Collectors.toList());

            workers.addAll(resFutures);

            metaConn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return workers;
    }

    @Override
    protected Loader<YCSBBenchmark> makeLoaderImpl() throws SQLException {
        return new YCSBLoader(this);
    }

    @Override
    protected Package getProcedurePackageImpl() {
        // TODO Auto-generated method stub
        return InsertRecord.class.getPackage();
    }

}
